import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { GameService, Game } from '../../services/game.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  games = signal<Game[]>([]);
  newGameName = signal('');
  loading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');
  showCreateForm = signal(false);

  constructor(
    private router: Router,
    private gameService: GameService,
    private userService: UserService
  ) {}

  ngOnInit() {
    // Redirect to login if not authenticated
    if (!this.userService.isLoggedIn()) {
      this.router.navigate(['/']);
      return;
    }

    this.loadGames();
  }

  loadGames() {
    this.loading.set(true);
    this.gameService.listGames().subscribe({
      next: (games) => {
        this.games.set(games);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading games:', error);
        this.errorMessage.set('Failed to load games');
        this.loading.set(false);
      }
    });
  }

  toggleCreateForm() {
    this.showCreateForm.set(!this.showCreateForm());
    this.newGameName.set('');
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  createGame() {
    const user = this.userService.getUser();
    if (!user) {
      this.errorMessage.set('You must be logged in to create a game');
      return;
    }

    if (!this.newGameName().trim()) {
      this.errorMessage.set('Please enter a game name');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.gameService.createGame({
      gameName: this.newGameName(),
      userId: user.userId
    }).subscribe({
      next: (game) => {
        this.successMessage.set(`Game "${game.gameName}" created successfully!`);
        this.newGameName.set('');
        this.showCreateForm.set(false);
        this.loadGames();
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: (error) => {
        console.error('Error creating game:', error);
        this.errorMessage.set('Failed to create game');
        this.loading.set(false);
      }
    });
  }

  joinGame(gameId: number) {
    const user = this.userService.getUser();
    if (!user) {
      this.errorMessage.set('You must be logged in to join a game');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.gameService.joinGame(gameId, { userId: user.userId }).subscribe({
      next: (game) => {
        this.successMessage.set(`Successfully joined "${game.gameName}"!`);
        this.loadGames();
        setTimeout(() => this.successMessage.set(''), 3000);
      },
      error: (error) => {
        console.error('Error joining game:', error);
        this.errorMessage.set('Failed to join game. You may already be in this game.');
        this.loading.set(false);
      }
    });
  }

  logout() {
    this.userService.clearUser();
    this.router.navigate(['/']);
  }

  get currentUser() {
    return this.userService.getUser();
  }
}
