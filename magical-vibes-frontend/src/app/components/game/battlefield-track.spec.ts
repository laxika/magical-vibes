import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';

/**
 * Regression test for the "Shriekgeist appears tapped when Mogg Fanatic leaves the
 * battlefield" bug. The battlefield creature loops in game.component.html render each
 * card with a card-display whose rotation follows [class.tapped]="ip.perm.tapped".
 *
 * They previously used `track ip.originalIndex` (array position). When a permanent in the
 * middle of the battlefield leaves, every later permanent's originalIndex shifts, so
 * Angular re-keyed the rendered card by position and reused the node/instance that had been
 * showing a *tapped* permanent for a different, untapped creature — leaving it visually
 * rotated mid-transition. The fix tracks by `ip.perm.id`.
 *
 * These hosts mirror that loop with tiny divs so we can assert on node identity, which is
 * what actually distinguishes the two tracking strategies (the [class.tapped] binding alone
 * self-corrects on the next check).
 */

interface Ip {
  perm: { id: string; tapped: boolean };
  originalIndex: number;
}

@Component({
  selector: 'app-track-by-id-host',
  template: `@for (ip of items(); track ip.perm.id) {
    <div class="cell" [attr.data-id]="ip.perm.id" [class.tapped]="ip.perm.tapped"></div>
  }`,
})
class TrackByIdHost {
  items = signal<Ip[]>([]);
}

@Component({
  selector: 'app-track-by-index-host',
  template: `@for (ip of items(); track ip.originalIndex) {
    <div class="cell" [attr.data-id]="ip.perm.id" [class.tapped]="ip.perm.tapped"></div>
  }`,
})
class TrackByIndexHost {
  items = signal<Ip[]>([]);
}

// Battlefield before removal: a tapped creature sits ahead of an untapped one.
function before(): Ip[] {
  return [
    { perm: { id: 'mogg', tapped: true }, originalIndex: 0 },
    { perm: { id: 'shriek', tapped: false }, originalIndex: 1 },
  ];
}

// Mogg Fanatic (tapped, index 0) leaves; Shriekgeist shifts down to index 0.
function after(): Ip[] {
  return [{ perm: { id: 'shriek', tapped: false }, originalIndex: 0 }];
}

describe('battlefield @for tracking (tapped card follows identity, not position)', () => {
  it('track ip.perm.id keeps the surviving creature on its own DOM node and untapped', () => {
    const fixture = TestBed.createComponent(TrackByIdHost);
    fixture.componentInstance.items.set(before());
    fixture.detectChanges();

    const shriekBefore = fixture.nativeElement.querySelector('[data-id="shriek"]');
    expect(shriekBefore).toBeTruthy();

    fixture.componentInstance.items.set(after());
    fixture.detectChanges();

    const shriekAfter = fixture.nativeElement.querySelector('[data-id="shriek"]');
    // Same identity -> same node reused; no stale rotation carried over from Mogg's node.
    expect(shriekAfter).toBe(shriekBefore);
    expect(shriekAfter.classList.contains('tapped')).toBe(false);
  });

  it('track ip.originalIndex (the old, buggy key) re-binds Shriekgeist onto the departed creature node', () => {
    const fixture = TestBed.createComponent(TrackByIndexHost);
    fixture.componentInstance.items.set(before());
    fixture.detectChanges();

    const shriekBefore = fixture.nativeElement.querySelector('[data-id="shriek"]');
    const moggBefore = fixture.nativeElement.querySelector('[data-id="mogg"]');

    fixture.componentInstance.items.set(after());
    fixture.detectChanges();

    const shriekAfter = fixture.nativeElement.querySelector('[data-id="shriek"]');
    // Index tracking reused index 0 (Mogg's node) for Shriekgeist and destroyed Shriekgeist's
    // original node — exactly the reuse that produced the stale rotation artifact.
    expect(shriekAfter).toBe(moggBefore);
    expect(shriekAfter).not.toBe(shriekBefore);
  });
});
