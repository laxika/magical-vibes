import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it } from 'vitest';
import { PlanarPanelComponent } from './planar-panel.component';
import { Card, PlanechaseView } from '../../../services/websocket.service';

afterEach(() => TestBed.resetTestingModule());

describe('Planar panel browser layout', () => {
  for (const width of [360, 1000]) {
    it('keeps the plane and roll control readable at ' + width + ' pixels', async () => {
      const fixture = TestBed.createComponent(PlanarPanelComponent);
      fixture.componentRef.setInput('state', {
        faceUp: [{ id: 'plane', card: { name: 'Panopticon', type: 'PLANE', subtypes: ['MIRRODIN'],
          cardText: 'When you planeswalk to Panopticon, draw a card.\nAt the beginning of your draw step, draw an additional card.\nWhenever chaos ensues, draw a card.',
          activatedAbilities: [] } as unknown as Card, counters: {} }],
        controllerId: 'me', deckSize: 19, rollCost: 0, canRoll: true, canPayRoll: true,
        lastRoll: null, rollSequence: 0
      } as PlanechaseView);
      const element = fixture.nativeElement as HTMLElement;
      element.style.width = width + 'px';
      document.body.appendChild(element);
      fixture.detectChanges();
      await fixture.whenStable();
      const button = element.querySelector('button')!;
      button.focus();
      expect(document.activeElement).toBe(button);
      expect(button.getBoundingClientRect().width).toBeGreaterThan(100);
      expect(element.scrollWidth).toBeLessThanOrEqual(width + 1);
      fixture.destroy();
    });
  }
});
