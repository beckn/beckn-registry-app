module.exports = {
  content: ["../templates/**/html/**/*.{html,js,md}", "../templates/**/markdown/**/*.{html,js,md}", "../scripts/application.js" , "../templates/css/_global.css"],
  safelist: [ "offset-8", "offset-5", "offset-4" , "offset-3", "offset-2", "text-right", "justify-center","h-2",
    "lg:offset-5", "lg:offset-4" ,"lg:offset-3","lg:offset-2",
    "sm:offset-5", "sm:offset-4","sm:offset-3","sm:offset-2",
    "col-2", "col-4", "col-6", "col-8", "col-12",
    "lg:col-2", "lg:col-4", "lg:col-6", "lg:col-8", "lg:col-12",
    "sm:col-2", "sm:col-4", "sm:col-6", "sm:col-8", "sm:col-12"],
  theme: {
    screens: {
      sm: "540px",
      // => @media (min-width: 576px) { ... }

      md: "720px",
      // => @media (min-width: 768px) { ... }

      lg: "960px",
      // => @media (min-width: 992px) { ... }

      xl: "1140px",
      // => @media (min-width: 1200px) { ... }

      "2xl": "1320px",
      // => @media (min-width: 1400px) { ... }
    },
    container: {
      center: true,
      padding: "16px",
    },
    extend: {
      colors: {
        black: "#212b36",
        dark: "#090E34",
        "dark-700": "#090e34b3",
        primary: "#3056D3",
        secondary: "#13C296",
        "body-color": "#637381",
        warning: "#FBBF24",
      },
      boxShadow: {
        input: "0px 7px 20px rgba(0, 0, 0, 0.03)",
        pricing: "0px 39px 23px -27px rgba(0, 0, 0, 0.04)",
        "switch-1": "0px 0px 5px rgba(0, 0, 0, 0.15)",
        testimonial: "0px 60px 120px -20px #EBEFFD",
      },
    },
  },
  variants: {
    extend: {},
  },
  plugins: [],
}
