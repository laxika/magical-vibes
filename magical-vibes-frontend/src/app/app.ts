import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CardPreviewService } from './services/card-preview.service';
import { CardDisplayComponent } from './components/game/card-display/card-display.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CardDisplayComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('magical-vibes-frontend');
  protected readonly cardPreview = inject(CardPreviewService);

  /** Zoom for the phone long-press preview: as large as fits the viewport
      (with breathing room), capped near the side panel's 1.6 preview scale. */
  get phonePreviewZoom(): number {
    const zoom = Math.min(1.7, (window.innerWidth * 0.85) / 165, (window.innerHeight * 0.8) / 231);
    return Math.max(zoom, 1);
  }
}
