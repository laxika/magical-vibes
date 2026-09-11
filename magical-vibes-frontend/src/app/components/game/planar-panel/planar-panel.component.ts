import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card, PlanechaseView } from '../../../services/websocket.service';

@Component({
  selector: 'app-planar-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './planar-panel.component.html',
  styleUrl: './planar-panel.component.css'
})
export class PlanarPanelComponent {
  state = input.required<PlanechaseView>();
  busy = input(false);
  roll = output<void>();
  cardHover = output<Card>();
  cardHoverEnd = output<void>();
  activate = output<{ sourceId: string; abilityIndex: number }>();
}
