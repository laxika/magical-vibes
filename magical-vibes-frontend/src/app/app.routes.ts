import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './components/home/home.component';
import { GameComponent } from './components/game/game.component';
import { DraftComponent } from './components/draft/draft.component';
import { CardBrowserComponent } from './components/card-browser/card-browser.component';
import { DeckBuilderComponent } from './components/deck-builder/deck-builder.component';
import { TutorialComponent } from './components/tutorial/tutorial.component';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'home', component: HomeComponent },
  { path: 'game', component: GameComponent },
  { path: 'draft', component: DraftComponent },
  { path: 'cards', component: CardBrowserComponent },
  { path: 'deck-builder', component: DeckBuilderComponent },
  { path: 'tutorial', component: TutorialComponent },
  { path: '**', redirectTo: '' }
];
