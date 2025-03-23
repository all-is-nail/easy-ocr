// Translations for the application
const translations = {
    en: {
        // index.html
        "main_title": "AI Driven Development",
        "clock_subtitle": "Check the current time across different timezones",
        "timezone_label": "Timezone:",
        "features_title": "Available Features",
        "ocr_feature": "OCR - Text Extraction from Images",
        "footer_copyright": "© 2025 AI Driven Development",
        
        // ocr.html
        "ocr_title": "Easy OCR",
        "ocr_subtitle": "Upload an image to extract text",
        "mode": "Mode:",
        "generic_ocr": "Generic OCR",
        "doc_processing": "Document Processing",
        "go_document": "Go to dedicated Document Processing page",
        "drag_drop": "Drag & drop image here",
        "or": "or",
        "choose_file": "Choose File",
        "paste_image": "Paste image (Cmd/Ctrl+V)",
        "process_image": "Process Image",
        "extracted_text": "Extracted Text",
        "copied": "Copied to clipboard!",
        
        // document.html
        "doc_title": "Document Field Extraction",
        "doc_subtitle": "Upload an ID card, driver's license, or other document to extract structured data",
        "switch_generic": "Switch to generic OCR mode",
        "drag_drop_doc": "Drag & drop document image here",
        "process_document": "Process Document",
        "extracted_fields": "Extracted Document Fields"
    },
    zh: {
        // index.html
        "main_title": "AI 驱动开发",
        "clock_subtitle": "查看不同时区的当前时间",
        "timezone_label": "时区：",
        "features_title": "可用功能",
        "ocr_feature": "OCR - 从图像中提取文本",
        "footer_copyright": "© 2025 AI 驱动开发",
        
        // ocr.html
        "ocr_title": "简易 OCR",
        "ocr_subtitle": "上传图像以提取文本",
        "mode": "模式：",
        "generic_ocr": "通用 OCR",
        "doc_processing": "文档处理",
        "go_document": "前往专用文档处理页面",
        "drag_drop": "将图像拖放至此",
        "or": "或",
        "choose_file": "选择文件",
        "paste_image": "粘贴图像 (Cmd/Ctrl+V)",
        "process_image": "处理图像",
        "extracted_text": "提取的文本",
        "copied": "已复制到剪贴板！",
        
        // document.html
        "doc_title": "文档字段提取",
        "doc_subtitle": "上传身份证、驾照或其他文档以提取结构化数据",
        "switch_generic": "切换到通用 OCR 模式",
        "drag_drop_doc": "将文档图像拖放至此",
        "process_document": "处理文档",
        "extracted_fields": "提取的文档字段"
    }
};

// Get browser language
function getBrowserLanguage() {
    const browserLang = navigator.language || navigator.userLanguage;
    
    // If we have the language manifest, check if browser language is supported
    if (typeof languageManifest !== 'undefined') {
        // Check for exact match first
        if (languageManifest.isSupported(browserLang)) {
            return browserLang;
        }
        
        // Then check for language base match (e.g., 'zh-CN' matches 'zh')
        const langBase = browserLang.split('-')[0];
        if (languageManifest.isSupported(langBase)) {
            return langBase;
        }
        
        // Return default language if no match
        return languageManifest.getDefaultLanguage();
    }
    
    // Fallback behavior if manifest is not available
    if (browserLang.startsWith('zh')) {
        return 'zh';
    }
    return 'en'; // Default to English
}

// Get user preferred language from localStorage or browser setting
function getPreferredLanguage() {
    const savedLang = localStorage.getItem('preferred_language');
    if (savedLang) {
        return savedLang;
    }
    return getBrowserLanguage();
}

// Set the user's preferred language
function setPreferredLanguage(lang) {
    localStorage.setItem('preferred_language', lang);
    return lang;
}

// Get translation for a key
function t(key) {
    const lang = getPreferredLanguage();
    const translation = translations[lang] && translations[lang][key];
    return translation || translations['en'][key] || key;
}

// Initialize language
function initLanguage() {
    const lang = getPreferredLanguage();
    applyLanguage(lang);
    updateLanguageToggle(lang);
}

// Apply translations to the page
function applyLanguage(lang) {
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        
        if (key) {
            // Handle placeholder attribute for inputs
            if (element.hasAttribute('placeholder')) {
                element.setAttribute('placeholder', translations[lang][key] || key);
            } 
            // Handle value attribute for input buttons
            else if (element.tagName === 'INPUT' && (element.type === 'button' || element.type === 'submit')) {
                element.value = translations[lang][key] || key;
            }
            // Handle regular buttons with textContent
            else if (element.tagName === 'BUTTON') {
                element.textContent = translations[lang][key] || key;
            }
            // Handle regular text content
            else {
                element.textContent = translations[lang][key] || key;
            }
        }
    });
    
    // Trigger custom event for language change
    const event = new CustomEvent('languageChanged', { detail: { language: lang } });
    document.dispatchEvent(event);
}

// Switch language
function switchLanguage(lang) {
    setPreferredLanguage(lang);
    applyLanguage(lang);
    updateLanguageToggle(lang);
}

// Update language toggle state
function updateLanguageToggle(lang) {
    const toggle = document.getElementById('language-toggle');
    if (toggle) {
        // If language manifest is available, use native names from there
        if (typeof languageManifest !== 'undefined') {
            const nextLang = lang === 'en' ? 'zh' : 'en';
            const nextLangInfo = languageManifest.getLanguage(nextLang);
            toggle.textContent = nextLangInfo ? nextLangInfo.nativeName : (nextLang === 'en' ? 'English' : '中文');
        } else {
            toggle.textContent = lang === 'en' ? '中文' : 'English';
        }
        toggle.setAttribute('onclick', `switchLanguage('${lang === 'en' ? 'zh' : 'en'}')`);
    }
}

// Add language toggle to the page
function addLanguageToggle() {
    const container = document.createElement('div');
    container.className = 'language-toggle-container';
    
    const toggle = document.createElement('button');
    toggle.id = 'language-toggle';
    toggle.className = 'language-toggle';
    
    const currentLang = getPreferredLanguage();
    
    // If language manifest is available, use native names from there
    if (typeof languageManifest !== 'undefined') {
        const nextLang = currentLang === 'en' ? 'zh' : 'en';
        const nextLangInfo = languageManifest.getLanguage(nextLang);
        toggle.textContent = nextLangInfo ? nextLangInfo.nativeName : (nextLang === 'en' ? 'English' : '中文');
    } else {
        toggle.textContent = currentLang === 'en' ? '中文' : 'English';
    }
    
    toggle.setAttribute('onclick', `switchLanguage('${currentLang === 'en' ? 'zh' : 'en'}')`);
    
    container.appendChild(toggle);
    document.body.appendChild(container);
}

// Initialize when the DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    addLanguageToggle();
    initLanguage();
}); 