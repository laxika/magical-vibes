import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './components/home/home.component';
import { GameComponent } from './components/game/game.component';
import { DraftComponent } from './components/draft/draft.component';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'home', component: HomeComponent },
  { path: 'game', component: GameComponent },
  { path: 'draft', component: DraftComponent },
  { path: '**', redirectTo: '' }
];
