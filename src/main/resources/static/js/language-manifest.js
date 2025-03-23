// Language manifest - lists all available languages for the application
const languageManifest = {
    languages: [
        {
            code: 'en',
            name: 'English',
            nativeName: 'English',
            default: true
        },
        {
            code: 'zh',
            name: 'Chinese',
            nativeName: '中文',
            default: false
        }
    ],
    
    // Get the default language code
    getDefaultLanguage: function() {
        const defaultLang = this.languages.find(lang => lang.default);
        return defaultLang ? defaultLang.code : 'en';
    },
    
    // Get a language by its code
    getLanguage: function(code) {
        return this.languages.find(lang => lang.code === code);
    },
    
    // Check if a language code is supported
    isSupported: function(code) {
        return this.languages.some(lang => lang.code === code);
    }
}; 