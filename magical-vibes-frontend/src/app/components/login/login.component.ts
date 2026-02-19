import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { WebsocketService, MessageType } from '../../services/websocket.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = signal('');
  password = signal('');
  loading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  constructor(
    private websocketService: WebsocketService,
    private router: Router
  ) {}

  onSubmit() {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (!this.username() || !this.password()) {
      this.errorMessage.set('Please enter both username and password');
      return;
    }

    this.loading.set(true);

    this.websocketService.login(this.username(), this.password()).subscribe({
      next: (response) => {
        this.loading.set(false);

        if (response.type === MessageType.LOGIN_SUCCESS) {
          let destination = '/home';
          let msg = response.message;
          if (response.activeGame) {
            destination = '/game';
            msg = 'Rejoining game...';
          } else if (response.activeDraftId) {
            destination = '/draft';
            msg = 'Rejoining draft...';
          }
          this.successMessage.set(msg);
          setTimeout(() => this.router.navigate([destination]), 1000);
        } else if (response.type === MessageType.LOGIN_FAILURE) {
          this.errorMessage.set(response.message);
        } else if (response.type === MessageType.TIMEOUT) {
          this.errorMessage.set(response.message);
        } else {
          this.errorMessage.set('Unexpected response from server');
        }
      },
      error: (error) => {
        this.loading.set(false);
        this.errorMessage.set(error || 'Connection error. Please try again.');
      }
    });
  }
}
