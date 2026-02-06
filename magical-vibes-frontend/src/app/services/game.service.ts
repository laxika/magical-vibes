import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Game {
  id: number;
  gameName: string;
  createdByUsername: string;
  status: string;
  createdAt: string;
  playerCount: number;
}

export interface CreateGameRequest {
  gameName: string;
  userId: number;
}

export interface JoinGameRequest {
  userId: number;
}

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly API_URL = 'http://localhost:8080/api/games';

  constructor(private http: HttpClient) { }

  createGame(request: CreateGameRequest): Observable<Game> {
    return this.http.post<Game>(this.API_URL, request);
  }

  listGames(): Observable<Game[]> {
    return this.http.get<Game[]>(this.API_URL);
  }

  joinGame(gameId: number, request: JoinGameRequest): Observable<Game> {
    return this.http.post<Game>(`${this.API_URL}/${gameId}/join`, request);
  }
}
