import { Injectable, signal } from '@angular/core';

export interface User {
  userId: number;
  username: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private currentUser = signal<User | null>(null);

  constructor() {
    // Load user from sessionStorage on service initialization
    const storedUser = sessionStorage.getItem('currentUser');
    if (storedUser) {
      this.currentUser.set(JSON.parse(storedUser));
    }
  }

  setUser(user: User) {
    this.currentUser.set(user);
    sessionStorage.setItem('currentUser', JSON.stringify(user));
  }

  getUser(): User | null {
    return this.currentUser();
  }

  clearUser() {
    this.currentUser.set(null);
    sessionStorage.removeItem('currentUser');
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }
}
