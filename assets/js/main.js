/**
 * DIVINE MANDATE BIBLE INSTITUTE (DIMABIN)
 * Global Javascript Actions
 */

document.addEventListener('DOMContentLoaded', () => {
  // Sticky navigation scrolling effect
  const header = document.querySelector('header');
  
  const handleScroll = () => {
    if (window.scrollY > 50) {
      header.classList.add('scrolled');
    } else {
      header.classList.remove('scrolled');
    }
  };

  window.addEventListener('scroll', handleScroll);
  // Run once on load in case page is already scrolled
  handleScroll();

  // Mobile menu functionality
  const mobileToggle = document.getElementById('mobileToggle');
  const mobileMenu = document.getElementById('mobileMenu');
  const mobileMenuOverlay = document.getElementById('mobileMenuOverlay');

  if (mobileToggle && mobileMenu && mobileMenuOverlay) {
    const toggleMenu = () => {
      mobileToggle.classList.toggle('active');
      mobileMenu.classList.toggle('active');
      mobileMenuOverlay.classList.toggle('active');
      
      // Prevent body scrolling when menu is active
      if (mobileMenu.classList.contains('active')) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = '';
      }
    };

    mobileToggle.addEventListener('click', toggleMenu);
    mobileMenuOverlay.addEventListener('click', toggleMenu);

    // Close menu when a link is clicked (excluding dropdown toggles)
    const mobileLinks = mobileMenu.querySelectorAll('.nav-link');
    mobileLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        if (link.classList.contains('mobile-dropdown-toggle')) {
          return;
        }
        mobileToggle.classList.remove('active');
        mobileMenu.classList.remove('active');
        mobileMenuOverlay.classList.remove('active');
        document.body.style.overflow = '';
      });
    });
  }

  // Mobile Dropdown Toggle
  const mobileDropdownToggle = document.querySelector('.mobile-dropdown-toggle');
  const mobileSubmenu = document.querySelector('.mobile-submenu');
  if (mobileDropdownToggle && mobileSubmenu) {
    mobileDropdownToggle.addEventListener('click', (e) => {
      e.preventDefault();
      const isHidden = window.getComputedStyle(mobileSubmenu).display === 'none';
      mobileSubmenu.style.display = isHidden ? 'flex' : 'none';
      const icon = mobileDropdownToggle.querySelector('i');
      if (icon) {
        if (isHidden) {
          icon.classList.replace('fa-chevron-down', 'fa-chevron-up');
        } else {
          icon.classList.replace('fa-chevron-up', 'fa-chevron-down');
        }
      }
    });
  }

  // Handle coming soon links in the entire navigation
  const comingSoonLinks = document.querySelectorAll('.coming-soon');
  comingSoonLinks.forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      // Remove any badges or icons from the text to get a clean name
      let cleanText = link.innerHTML;
      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = cleanText;
      let textContent = tempDiv.textContent || tempDiv.innerText || "";
      textContent = textContent.replace('Soon', '').trim();
      
      showToast(`${textContent} is coming soon!`);
    });
  });

  // Custom Elegant Toast Notification
  function showToast(message) {
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
      toastContainer = document.createElement('div');
      toastContainer.id = 'toast-container';
      document.body.appendChild(toastContainer);
    }

    const toast = document.createElement('div');
    toast.className = 'custom-toast';
    toast.innerHTML = `<i class="fa-solid fa-bell" style="color: var(--accent);"></i> <span>${message}</span>`;
    toastContainer.appendChild(toast);

    // Trigger animation
    setTimeout(() => {
      toast.classList.add('show');
    }, 10);

    // Remove toast after 3.5s
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => {
        toast.remove();
      }, 300);
    }, 3500);
  }
});
