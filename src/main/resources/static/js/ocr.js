document.addEventListener('DOMContentLoaded', function() {
    // DOM elements
    const dropArea = document.getElementById('drop-area');
    const fileInput = document.getElementById('file-input');
    const imagePreviewContainer = document.getElementById('image-preview-container');
    const imagePreview = document.getElementById('image-preview');
    const removeImageBtn = document.getElementById('remove-image');
    const processBtn = document.getElementById('process-btn');
    const resultContainer = document.getElementById('result-container');
    const resultJson = document.getElementById('result-json');
    const copyNotification = document.getElementById('copy-notification');
    const spinner = document.getElementById('spinner');
    
    let currentFile = null; // Store the current file
    
    // Prevent defaults for drag events
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropArea.addEventListener(eventName, preventDefaults, false);
        document.body.addEventListener(eventName, preventDefaults, false);
    });
    
    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }
    
    // Highlight drop area when item is dragged over it
    ['dragenter', 'dragover'].forEach(eventName => {
        dropArea.addEventListener(eventName, highlight, false);
    });
    
    ['dragleave', 'drop'].forEach(eventName => {
        dropArea.addEventListener(eventName, unhighlight, false);
    });
    
    function highlight() {
        dropArea.classList.add('highlight');
    }
    
    function unhighlight() {
        dropArea.classList.remove('highlight');
    }
    
    // Handle dropped files
    dropArea.addEventListener('drop', handleDrop, false);
    
    function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        
        if (files.length) {
            handleFiles(files);
        }
    }
    
    // Setup the file input
    fileInput.addEventListener('change', function() {
        if (fileInput.files.length) {
            handleFiles(fileInput.files);
        }
    });
    
    // Click on drop area to trigger file input
    dropArea.addEventListener('click', function() {
        fileInput.click();
    });
    
    // Handle files from file input or drop
    function handleFiles(files) {
        if (files[0].type.startsWith('image/')) {
            showPreview(files[0]);
        } else {
            showError('Please select an image file');
        }
    }
    
    // Handle paste event
    document.addEventListener('paste', function(e) {
        const items = (e.clipboardData || e.originalEvent.clipboardData).items;
        
        for (let i = 0; i < items.length; i++) {
            if (items[i].type.indexOf('image') !== -1) {
                const file = items[i].getAsFile();
                showPreview(file);
                break;
            }
        }
    });
    
    // Show image preview
    function showPreview(file) {
        currentFile = file;
        console.log("Selected file:", file.name, "Size:", file.size, "Type:", file.type);
        
        const reader = new FileReader();
        reader.onload = function(e) {
            imagePreview.src = e.target.result;
            dropArea.style.display = 'none';
            imagePreviewContainer.style.display = 'block';
            processBtn.disabled = false;
            resultContainer.style.display = 'none';
        };
        reader.readAsDataURL(file);
    }
    
    // Remove image
    removeImageBtn.addEventListener('click', function(e) {
        e.stopPropagation(); // Prevent triggering the dropArea click
        resetImage();
    });
    
    function resetImage() {
        imagePreview.src = '';
        fileInput.value = '';
        currentFile = null;
        imagePreviewContainer.style.display = 'none';
        dropArea.style.display = 'flex';
        processBtn.disabled = true;
        resultContainer.style.display = 'none';
    }
    
    // Show error message in the result container
    function showError(message) {
        console.error("Error:", message);
        spinner.style.display = 'none';
        resultContainer.style.display = 'block';
        resultJson.innerHTML = `<div class="error-message">${message}</div>`;
        resultContainer.scrollIntoView({ behavior: 'smooth' });
    }
    
    // Process the image
    processBtn.addEventListener('click', processImage);
    
    function processImage() {
        if (!currentFile) return;
        
        console.log("Processing image:", currentFile.name);
        spinner.style.display = 'flex';
        resultContainer.style.display = 'none';
        
        // If file is from paste or has been read as data URL
        if (imagePreview.src.indexOf('data:image') === 0) {
            console.log("Processing as base64 image");
            const base64Image = imagePreview.src;
            processBase64Image(base64Image);
        } else {
            console.log("Processing as file upload");
            // If file is from file input
            const formData = new FormData();
            formData.append('image', currentFile);
            
            fetch('/api/ocr/process', {
                method: 'POST',
                body: formData
            })
            .then(handleResponse)
            .then(displayResult)
            .catch(handleError);
        }
    }
    
    function processBase64Image(base64Image) {
        console.log("Sending base64 image to API, length:", base64Image.length);
        
        fetch('/api/ocr/process-base64', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ image: base64Image })
        })
        .then(handleResponse)
        .then(displayResult)
        .catch(handleError);
    }
    
    function handleResponse(response) {
        console.log("Received response with status:", response.status);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return response.json();
    }
    
    function handleError(error) {
        console.error('Error during processing:', error);
        showError(`An error occurred during processing: ${error.message}`);
        spinner.style.display = 'none';
    }
    
    function displayResult(data) {
        console.log("Processing result:", data);
        spinner.style.display = 'none';
        resultContainer.style.display = 'block';
        
        // Check if the response contains an error
        if (data.error) {
            showError(data.error);
            return;
        }
        
        // Check for empty or unhelpful response
        if (data.extracted_text && data.extracted_text.includes("there's no image attached")) {
            showError("The API couldn't process the image properly. Please try a different image or format.");
            return;
        }
        
        // Format the JSON for display
        try {
            resultJson.textContent = JSON.stringify(data, null, 2);
        } catch (e) {
            showError(`Error formatting result: ${e.message}`);
            return;
        }
        
        // Scroll to result
        resultContainer.scrollIntoView({ behavior: 'smooth' });
    }
    
    // Copy JSON to clipboard when clicked
    resultJson.addEventListener('click', function() {
        const text = resultJson.textContent;
        navigator.clipboard.writeText(text)
            .then(() => {
                showCopyNotification();
            })
            .catch(err => {
                console.error('Failed to copy: ', err);
            });
    });
    
    function showCopyNotification() {
        copyNotification.classList.add('show');
        setTimeout(() => {
            copyNotification.classList.remove('show');
        }, 2000);
    }
}); 