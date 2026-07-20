import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { WebsocketService, MessageType } from '../../services/websocket.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  username = signal('');
  password = signal('');
  confirmPassword = signal('');
  loading = signal(false);
  errorMessage = signal('');

  constructor(
    private websocketService: WebsocketService,
    private router: Router
  ) {}

  onSubmit() {
    this.errorMessage.set('');

    const username = this.username().trim();
    const password = this.password();
    const confirmPassword = this.confirmPassword();

    if (!username || !password || !confirmPassword) {
      this.errorMessage.set('Please fill in all fields');
      return;
    }

    if (password !== confirmPassword) {
      this.errorMessage.set('Passwords do not match');
      return;
    }

    this.loading.set(true);

    this.websocketService.register(username, password, confirmPassword).subscribe({
      next: (response) => {
        if (response.type === MessageType.REGISTER_SUCCESS) {
          this.logInAfterRegistration(username, password);
        } else {
          this.loading.set(false);
          this.errorMessage.set(response.message || 'Registration failed');
        }
      },
      error: (error) => {
        this.loading.set(false);
        this.errorMessage.set(error || 'Connection error. Please try again.');
      },
      complete: () => {
        if (this.loading() && !this.errorMessage()) {
          this.loading.set(false);
        }
      }
    });
  }

  private logInAfterRegistration(username: string, password: string) {
    this.websocketService.login(username, password).subscribe({
      next: (response) => {
        this.loading.set(false);

        if (response.type === MessageType.LOGIN_SUCCESS) {
          this.router.navigate(['/home']);
        } else {
          // Account exists but auto-login failed; let the user log in manually
          this.router.navigate(['/']);
        }
      },
      error: () => {
        this.loading.set(false);
        this.router.navigate(['/']);
      }
    });
  }
}
