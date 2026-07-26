import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Card } from '../../../services/websocket.service';
import { CardPreviewService } from '../../../services/card-preview.service';
import { ScryfallCardDataService } from '../../../services/scryfall-card-data.service';
import { ScryfallImageService } from '../../../services/scryfall-image.service';
import { SetSymbolService } from '../../../services/set-symbol.service';
import { CardDisplayComponent } from './card-display.component';

/**
 * Mounts the real card frame in a real browser so its text box can actually be measured.
 *
 * <p>The component sizes its own text by reading back what the browser laid out, which means
 * every bug it has had was a bug about layout arriving late — a web font swapping in after the
 * fit, a mana symbol taking its width only once the face that draws it arrived. jsdom reports
 * every box as zero, so those are invisible to the default test target no matter how the
 * assertions are written; this harness exists so they are not.
 *
 * <p>Symbols are not faked. They are Mana font glyphs produced by a pure function, so there is
 * no service to stand in for and nothing to resolve — what a test renders is what a card
 * renders. Flavour text still arrives in two steps, and still has a fake, because that gap is
 * real and is where the remaining bugs live.
 */

/** Flavour text, artist and rarity arrive from a set-wide fetch, so they show up after mount. */
export class FakeScryfallCardDataService {
  private data = new Map<string, { flavorText: string | null; artist: string | null; rarity: string | null }>();

  getCardData(setCode: string, collectorNumber: string) {
    return this.data.get(`${setCode}/${collectorNumber}`) ?? null;
  }

  resolveCardData(setCode: string, collectorNumber: string,
                  flavorText: string | null, artist = 'Test Artist', rarity = 'COMMON'): void {
    this.data.set(`${setCode}/${collectorNumber}`, { flavorText, artist, rarity });
  }
}

/** Art is irrelevant to text fitting — the window is a fixed 96px whether it loads or not. */
export class FakeScryfallImageService {
  getCachedArtCropUrl(): string | null { return null; }
  getArtCropUrl(): Promise<string> { return Promise.reject(new Error('no art in tests')); }
}

export class FakeSetSymbolService {
  getSymbolUrl(): string | null { return null; }
}

export class FakeCardPreviewService {
  isPhoneLayout(): boolean { return false; }
  show(): void { /* no preview in tests */ }
  clear(): void { /* no preview in tests */ }
}

export interface MountedCard {
  fixture: ComponentFixture<CardDisplayComponent>;
  cardData: FakeScryfallCardDataService;
  /** The rules text box, the element whose overflow is the whole question. */
  textBox: HTMLElement;
  host: HTMLElement;
  /**
   * Runs every deferred thing a real card waits on — fonts, images, another change detection
   * pass — and returns once layout has settled, which is the only point at which measuring
   * proves anything.
   */
  settle(): Promise<void>;
}

export async function mountCard(card: Card): Promise<MountedCard> {
  const cardData = new FakeScryfallCardDataService();

  TestBed.configureTestingModule({
    imports: [CardDisplayComponent],
    providers: [
      { provide: ScryfallCardDataService, useValue: cardData },
      { provide: ScryfallImageService, useClass: FakeScryfallImageService },
      { provide: SetSymbolService, useClass: FakeSetSymbolService },
      { provide: CardPreviewService, useClass: FakeCardPreviewService },
    ],
  });

  const fixture = TestBed.createComponent(CardDisplayComponent);
  fixture.componentRef.setInput('card', card);
  fixture.detectChanges();

  const host = fixture.nativeElement as HTMLElement;

  /**
   * The fakes hand over data by plain method return, so nothing marks the component dirty the
   * way a resolving promise does under zone.js in the real app. Without the explicit mark,
   * Angular skips the refresh and only its verification pass sees the new value, which surfaces
   * as NG0100 rather than as the render being tested.
   */
  const sync = () => {
    fixture.componentRef.changeDetectorRef.markForCheck();
    fixture.detectChanges();
  };

  const settle = async () => {
    // The font wait is the bug this harness was built for, and it now covers the symbols too:
    // Cinzel and Crimson Text swapping in change the metrics of every line, and Mana arriving
    // is the difference between a symbol occupying its width and occupying nothing. The
    // component refits on its own once fonts land, and that refit is a promise callback of its
    // own, so the loop below gives it room to run rather than racing it.
    await document.fonts.ready;
    sync();
    const images = Array.from(host.querySelectorAll('img'));
    await Promise.all(images.map(img => img.decode().catch(() => undefined)));
    sync();
    for (let i = 0; i < 3; i++) {
      await new Promise<void>(resolve => requestAnimationFrame(() => resolve()));
      await Promise.resolve();
      sync();
    }
  };

  await settle();

  return {
    fixture,
    cardData,
    get textBox() { return host.querySelector('.text-box') as HTMLElement; },
    host,
    settle,
  } as MountedCard;
}

/** Overflow in the rules box, in pixels. Zero or less means nothing is clipped. */
export function overflowOf(el: HTMLElement): number {
  return el.scrollHeight - el.clientHeight;
}

/** Rendered font size of an element, in px. */
export function fontSizeOf(el: HTMLElement): number {
  return parseFloat(getComputedStyle(el).fontSize);
}

const BASE_CARD: Card = {
  id: 'test-card',
  name: 'Test Card',
  type: 'INSTANT',
  additionalTypes: [],
  supertypes: [],
  subtypes: [],
  cardText: null,
  manaCost: null,
  power: null,
  toughness: null,
  keywords: [],
  hasTapAbility: false,
  setCode: 'SOS',
  collectorNumber: '1',
  color: 'WHITE',
  colors: ['WHITE'],
  needsTarget: false,
  needsSpellTarget: false,
  activatedAbilities: [],
  loyalty: null,
  hasConvoke: false,
  hasPhyrexianMana: false,
  phyrexianManaCount: 0,
  token: false,
  watermark: null,
  hasAlternateCastingCost: false,
  alternateCostLifePayment: 0,
  alternateCostSacrificeCount: 0,
  alternateCostTapCount: 0,
  alternateCostReturnCount: 0,
  alternateCostManaCost: null,
  graveyardActivatedAbilities: [],
  transformable: false,
  kickerCost: null,
  modalChoicesRequired: 0,
  modalChoicesMax: 0,
  modalOptional: false,
  modalOptions: null,
  prepareSpell: null,
};

export function card(overrides: Partial<Card>): Card {
  return { ...BASE_CARD, ...overrides };
}
