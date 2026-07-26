import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Card } from '../../../services/websocket.service';
import { CardPreviewService } from '../../../services/card-preview.service';
import { ManaSymbolService } from '../../../services/mana-symbol.service';
import { ScryfallCardDataService } from '../../../services/scryfall-card-data.service';
import { ScryfallImageService } from '../../../services/scryfall-image.service';
import { SetSymbolService } from '../../../services/set-symbol.service';
import { WatermarkService } from '../../../services/watermark.service';
import { manaSymbolImg } from '../../../services/mana-symbol-markup';
import { CardDisplayComponent } from './card-display.component';

/**
 * Mounts the real card frame in a real browser so its text box can actually be measured.
 *
 * <p>The component sizes its own text by reading back what the browser laid out, which means
 * every bug it has had was a bug about layout arriving late — a web font swapping in after the
 * fit, a mana symbol taking width only once its image decoded. jsdom reports every box as zero,
 * so those are invisible to the default test target no matter how the assertions are written;
 * this harness exists so they are not.
 *
 * <p>Its fakes are deliberately faithful on the one axis that matters: they hand over content in
 * the same two steps the real services do, unresolved first and resolved later, because the gap
 * between those two steps is where the bugs live.
 */

/**
 * Stands in for ManaSymbolService with its real two-step behaviour: `{W}` stays literal text
 * until the symbol is available, then becomes an image and bumps the version signal. Uses the
 * production markup helper, so the img this renders is the img a card renders.
 */
export class FakeManaSymbolService {
  symbolsVersion = signal(0);
  private loaded = false;

  /**
   * A blob URL, exactly as the real service produces from its IndexedDB cache — and it has to be
   * both a blob and a fresh one per instance, for two separate reasons that each silently make
   * this harness useless.
   *
   * <p>A `data:` URI of the same SVG gets its intrinsic size synchronously, so an `<img>`
   * pointing at one has width on its very first layout and the load-order bug never happens.
   *
   * <p>And a URL shared between tests is served from the browser's image cache the second time
   * it is requested, which is synchronous too — so with a static URL only the first test in the
   * file measures an undecoded image, and even that one does not once another test has run
   * before it. Both mistakes were made here, and both showed up as tests passing against
   * markup already known to be broken.
   */
  readonly symbolUrl = URL.createObjectURL(new Blob(
      ['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 150 150">'
       + '<circle cx="75" cy="75" r="75" fill="#cccccc"/></svg>'],
      { type: 'image/svg+xml' }));

  replaceSymbols(text: string): string {
    this.symbolsVersion();
    return text.replace(/\{([^}]+)\}/g, match =>
        this.loaded ? manaSymbolImg(this.symbolUrl, match) : match);
  }

  getSymbolUrl(): string | null {
    return this.loaded ? this.symbolUrl : null;
  }

  /** The moment the real service reaches when its fetches land. */
  resolveSymbols(): void {
    this.loaded = true;
    this.symbolsVersion.update(v => v + 1);
  }
}

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

export class FakeWatermarkService {
  getCachedWatermarkUrl(): string | null { return null; }
  getWatermarkUrl(): Promise<string> { return Promise.reject(new Error('no watermark in tests')); }
}

export class FakeCardPreviewService {
  isPhoneLayout(): boolean { return false; }
  show(): void { /* no preview in tests */ }
  clear(): void { /* no preview in tests */ }
}

export interface MountedCard {
  fixture: ComponentFixture<CardDisplayComponent>;
  symbols: FakeManaSymbolService;
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
  const symbols = new FakeManaSymbolService();
  const cardData = new FakeScryfallCardDataService();

  TestBed.configureTestingModule({
    imports: [CardDisplayComponent],
    providers: [
      { provide: ManaSymbolService, useValue: symbols },
      { provide: ScryfallCardDataService, useValue: cardData },
      { provide: ScryfallImageService, useClass: FakeScryfallImageService },
      { provide: SetSymbolService, useClass: FakeSetSymbolService },
      { provide: WatermarkService, useClass: FakeWatermarkService },
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
    // Both waits are the bugs this harness was built for: the font swap changes text metrics,
    // and an image with no width until it decodes changes where a line wraps. The component
    // refits on its own once fonts land, and that refit is a promise callback of its own, so
    // the loop below gives it room to run rather than racing it.
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
    symbols,
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
