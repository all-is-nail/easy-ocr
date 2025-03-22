document.addEventListener('DOMContentLoaded', function() {
    const clockElement = document.getElementById('clock');
    const dateElement = document.getElementById('date');
    const timezoneSelect = document.getElementById('timezone');
    
    function updateClock() {
        const now = new Date();
        const offset = parseFloat(timezoneSelect.value);
        
        // Calculate time with timezone offset
        const utc = now.getTime() + (now.getTimezoneOffset() * 60000);
        const targetTime = new Date(utc + (3600000 * offset));
        
        // Format time
        const hours = targetTime.getHours().toString().padStart(2, '0');
        const minutes = targetTime.getMinutes().toString().padStart(2, '0');
        const seconds = targetTime.getSeconds().toString().padStart(2, '0');
        
        // Format date
        const options = { 
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric',
            timeZone: offset === 0 ? 'UTC' : undefined
        };
        
        clockElement.textContent = `${hours}:${minutes}:${seconds}`;
        dateElement.textContent = targetTime.toDateString();
    }
    
    // Update clock immediately and then every second
    updateClock();
    setInterval(updateClock, 1000);
    
    // Update when timezone changes
    timezoneSelect.addEventListener('change', updateClock);
}); 