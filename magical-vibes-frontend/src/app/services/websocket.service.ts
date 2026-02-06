import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface LoginResponse {
  type: string;
  message: string;
  userId?: number;
  username?: string;
}

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  private readonly WS_URL = 'ws://localhost:8080/ws/login';

  constructor() { }

  login(username: string, password: string): Observable<LoginResponse> {
    return new Observable(observer => {
      const ws = new WebSocket(this.WS_URL);

      ws.onopen = () => {
        console.log('WebSocket connection established');
        const loginRequest = {
          type: 'LOGIN',
          username: username,
          password: password
        };
        ws.send(JSON.stringify(loginRequest));
      };

      ws.onmessage = (event) => {
        console.log('Received message:', event.data);
        try {
          const response: LoginResponse = JSON.parse(event.data);
          observer.next(response);
          observer.complete();
        } catch (error) {
          observer.error('Failed to parse response');
        }
      };

      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        observer.error('WebSocket connection error');
      };

      ws.onclose = (event) => {
        console.log('WebSocket connection closed:', event.code, event.reason);
        if (!observer.closed) {
          observer.complete();
        }
      };

      // Cleanup on unsubscribe
      return () => {
        if (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING) {
          ws.close();
        }
      };
    });
  }
}
