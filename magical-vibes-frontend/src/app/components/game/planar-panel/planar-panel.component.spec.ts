import { TestBed } from '@angular/core/testing';
import { PlanarPanelComponent } from './planar-panel.component';
import { Card, PlanechaseView } from '../../../services/websocket.service';

function state(overrides: Partial<PlanechaseView> = {}): PlanechaseView {
  return {
    faceUp: [{ id: 'plane', card: { name: 'Panopticon', type: 'PLANE', subtypes: ['MIRRODIN'],
      cardText: 'Whenever chaos ensues, draw a card.', activatedAbilities: [] } as unknown as Card, counters: {} }],
    controllerId: 'me', deckSize: 19, rollCost: 0, canRoll: true, canPayRoll: true,
    lastRoll: null, lastRollPlayerId: null, rollSequence: 0, ...overrides
  };
}

describe('PlanarPanelComponent', () => {
  function mount(value = state()) {
    const fixture = TestBed.createComponent(PlanarPanelComponent);
    fixture.componentRef.setInput('state', value);
    fixture.detectChanges();
    return fixture;
  }

  it('shows the plane and emits a roll without inventing a result', () => {
    const fixture = mount();
    const roll = vi.fn();
    fixture.componentInstance.roll.subscribe(roll);
    expect(fixture.nativeElement.textContent).toContain('Panopticon');
    expect(fixture.nativeElement.textContent).toContain('Free');
    fixture.nativeElement.querySelector('button').click();
    expect(roll).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.querySelector('output').textContent.trim()).toBe('');
  });

  it('disables rolling when priority does not permit it', () => {
    const fixture = mount(state({ canRoll: false, rollCost: 2 }));
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('{2}');
  });

  it('disables duplicate actions while payment or submission is pending', () => {
    const fixture = mount();
    fixture.componentRef.setInput('busy', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(true);
  });

  it('renders all face-up planes and the authoritative last result after reconnect', () => {
    const first = state();
    const fixture = mount(state({ faceUp: [...first.faceUp, { ...first.faceUp[0], id: 'second' }],
      lastRoll: 'CHAOS', rollSequence: 7 }));
    expect(fixture.nativeElement.querySelectorAll('details')).toHaveLength(2);
    expect(fixture.nativeElement.querySelector('output').textContent).toContain('Chaos');
    fixture.componentRef.setInput('state', { ...fixture.componentInstance.state() });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('output')).toHaveLength(1);
  });

  it('shows the opening-hand message until a starting plane is revealed', () => {
    const fixture = mount(state({ faceUp: [], canRoll: false }));
    expect(fixture.nativeElement.textContent).toContain('opening hands');
  });
});
