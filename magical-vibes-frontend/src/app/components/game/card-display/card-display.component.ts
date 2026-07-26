import { Component, Input, HostBinding, OnInit, OnChanges, OnDestroy, AfterViewInit, AfterViewChecked, SimpleChanges, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Card, Permanent } from '../../../services/websocket.service';
import { CardPreviewService } from '../../../services/card-preview.service';
import { ScryfallImageService } from '../../../services/scryfall-image.service';
import { ScryfallCardDataService } from '../../../services/scryfall-card-data.service';
import { SetSymbolService } from '../../../services/set-symbol.service';
import { distinctGrantedAbilityTexts, formatEnumName, formatKeywords, formatTypeLine, hasCardType } from '../../../utils/format-utils';
import { manaSymbolHtml, watermarkSymbolClasses } from '../../../utils/mana-symbols';
import { largestFittingSize, renderedTextKey } from './card-text-fit';

export interface PlaneswalkerAbilityLine {
  /** Display cost, e.g. "+1", "−2", "0"; null for static ability lines. */
  cost: string | null;
  dir: 'up' | 'down' | 'zero' | null;
  html: SafeHtml;
}

@Component({
  selector: 'app-card-display',
  standalone: true,
  templateUrl: './card-display.component.html',
  styleUrl: './card-display.component.css',
  host: {
    'class': 'card',
    '(touchstart)': 'onTouchStart($event)',
    '(touchmove)': 'onTouchMove($event)',
    '(touchend)': 'onTouchEnd($event)',
    '(touchcancel)': 'onTouchCancel()',
    '(contextmenu)': 'onContextMenu($event)',
  }
})
export class CardDisplayComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit, AfterViewChecked {
  @Input({ required: true }) card!: Card;
  @Input() permanent: Permanent | null = null;
  @Input() preview = false;
  /** Which face's art to show — the card browser/deck builder pass 'back' for flipped double-faced cards. */
  @Input() artFace: 'front' | 'back' = 'front';

  formatKeywords = formatKeywords;
  formatEnumName = formatEnumName;
  artUrl = signal<string | null>(null);

  @ViewChild('textBox') textBoxRef?: ElementRef<HTMLDivElement>;
  @ViewChild('nameText') nameTextRef?: ElementRef<HTMLElement>;

  private static readonly MAX_FONT_SIZE = 11;
  private static readonly MIN_FONT_SIZE = 7;
  /** Name plate range. A printed card sets a long name in a smaller face rather than
   *  cutting it off, so the name shrinks before the ellipsis is ever reached. */
  private static readonly MAX_NAME_FONT_SIZE = 12;
  /** Lower than the rules text's floor, because an ellipsised name costs the player the one
   *  thing they need in order to identify the card at all. "Antiquities on the Loose" beside a
   *  three-symbol mana cost has about 91px to live in and needs roughly 6.2px to fit it; a 7px
   *  floor left it truncated, which is worse than small. Matches the planeswalker text floor. */
  private static readonly MIN_NAME_FONT_SIZE = 6;
  /** Planeswalker ability text may shrink further, like the denser print on real walker frames. */
  private static readonly PW_MIN_FONT_SIZE = 6;
  private static readonly FONT_STEP = 0.5;
  /** Flavour text is set as a fraction of the rules text, not a fixed step behind it. A flat
   *  2px reduction is a tenth off 20px type but a quarter off the 8px this box routinely
   *  lands on, which is where flavour stopped looking smaller and started looking broken. */
  private static readonly FLAVOR_FONT_RATIO = 0.92;
  /** Bisection target. A tenth of a pixel is finer than the eye resolves at these sizes, so
   *  it is effectively "as large as fits" without paying for a search that never converges. */
  private static readonly FONT_PRECISION = 0.1;
  /** The prepare spell's inset is a far narrower column than the rules text beside it,
   *  so it fits separately and may go smaller than the rules text ever does. */
  private static readonly PREPARE_FONT_RATIO = 0.94;
  private static readonly PREPARE_MIN_FONT_SIZE = 4.5;
  private lastTextFingerprint = '';
  private lastNameFingerprint = '';
  private destroyed = false;
  private contentObserver: MutationObserver | null = null;
  private pendingRefit = 0;

  private scryfallImageService = inject(ScryfallImageService);
  private scryfallCardDataService = inject(ScryfallCardDataService);
  private setSymbolService = inject(SetSymbolService);
  private cardPreviewService = inject(CardPreviewService);
  private sanitizer = inject(DomSanitizer);
  private hostRef = inject<ElementRef<HTMLElement>>(ElementRef);

  /* Long-press preview (phone layouts only): touch cards can't hover, so
     holding a finger on any card shows it in the fullscreen preview overlay
     and releasing dismisses it. A completed long-press suppresses the
     synthetic click (preventDefault on touchend) so previewing a card never
     also plays/targets it. */
  private static readonly LONG_PRESS_MS = 400;
  private static readonly LONG_PRESS_SLOP_PX = 12;
  private longPressTimer: ReturnType<typeof setTimeout> | null = null;
  private longPressFired = false;
  private touchStartX = 0;
  private touchStartY = 0;

  ngOnInit(): void {
    this.fetchCardArt();
    CardDisplayComponent.whenFontsReady().then(() => this.refitAfterFontSwap());
  }

  ngAfterViewInit(): void {
    this.observeRenderedContent();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.contentObserver?.disconnect();
    if (this.pendingRefit) cancelAnimationFrame(this.pendingRefit);
    this.cancelLongPress();
  }

  /**
   * Refits whenever the rendered content changes, which is the only signal that reliably
   * corresponds to "the thing I measure is now different".
   *
   * <p>The fitters used to run from ngAfterViewChecked alone, and in this app that hook fires
   * exactly once per card and then never again — it is zoneless (no zone.js, no
   * provideZoneChangeDetection), so nothing re-checks a view just because a fetch resolved. The
   * content a card still waits on lands after that single check: flavour text, artist and rarity
   * all arrive together from the set-wide fetch. The fit was therefore computed against a box
   * that had not finished filling, and the correction never ran. Three separate fixes landed in
   * code that could not execute.
   *
   * <p>Mana symbols used to be in that list too — they were literal `{W}` braces until each
   * symbol's SVG was fetched. They are font glyphs now and arrive with the first render, which
   * takes one of the two late-content sources away but not the hook: the font behind them can
   * still land late, and that is what whenFontsReady covers.
   *
   * <p>Attributes are deliberately not observed. The fitters work by writing style.fontSize and
   * style.display, both attribute mutations, so watching those would feed the observer its own
   * output forever. Node and text changes are the ones that alter layout, and the fitters never
   * make them.
   */
  private observeRenderedContent(): void {
    this.contentObserver = new MutationObserver(() => this.scheduleRefit());
    this.contentObserver.observe(this.hostRef.nativeElement,
        { childList: true, characterData: true, subtree: true });
  }

  /** Coalesces a burst of mutations into one fit, on the frame after they land. */
  private scheduleRefit(): void {
    if (this.pendingRefit || this.destroyed) return;
    this.pendingRefit = requestAnimationFrame(() => {
      this.pendingRefit = 0;
      if (this.destroyed) return;
      this.fitCardName();
      this.fitTextToBox();
    });
  }

  /**
   * The faces a card actually paints with, at the weights and styles it uses them at, each with
   * a character it must have a glyph for.
   *
   * <p>The probe is not decoration. `document.fonts.load()` filters candidate faces by whether
   * their unicode-range covers the text it is asked about, and it defaults to asking about a
   * space — which is fine for the two text faces and meaningless for Mana, whose every glyph is
   * a private-use codepoint. U+E600 is `{W}`, the first of them.
   */
  private static readonly FITTED_FONTS: ReadonlyArray<readonly [font: string, probe: string]> = [
    ['700 12px Cinzel', 'M'],
    ['400 11px "Crimson Text"', 'M'],
    ['italic 400 11px "Crimson Text"', 'M'],
    ['14px Mana', '\uE600'],
  ];
  private static fontsReady: Promise<unknown> | null = null;

  /**
   * Cinzel and Crimson Text are declared `font-display: swap` (src/fonts.css), so a paint
   * that happens before they load uses the Georgia/Times fallbacks and the real faces swap
   * in afterwards — at different metrics. Both fitters measure the rendered DOM, so whatever
   * they size before that swap is sized against the wrong font, and their fingerprints then
   * suppress the re-measure that would correct it: names ellipsised that had room to shrink,
   * rules and flavour text fitted to a box they no longer fit. Self-hosting and preloading
   * the two latin faces makes that window small enough to rarely be hit, but it cannot be
   * closed — a cold cache on a slow link still lands in it, and neither the italic nor the
   * latin-ext faces are preloaded at all. So this stays as the correction.
   *
   * <p>Mana is here for a related reason rather than the same one. It is `font-display: block`,
   * so a mana symbol is not painted in the wrong face before it arrives — it is not painted at
   * all, and an unpainted symbol is a hole in the line the width of whatever the fallback made
   * of a private-use codepoint. Either way the line measures at a width it will not keep, and
   * either way the fix is to measure it again once the face is really there.
   *
   * <p>`ready` is awaited first because `load()` only matches faces already declared, and the
   * rules arrive with the stylesheet. One promise serves every card on the table.
   */
  private static whenFontsReady(): Promise<unknown> {
    CardDisplayComponent.fontsReady ??= document.fonts.ready
      .then(() => Promise.all(
          CardDisplayComponent.FITTED_FONTS.map(([font, probe]) => document.fonts.load(font, probe))))
      .catch(() => undefined);
    return CardDisplayComponent.fontsReady;
  }

  /** Drops both fingerprints so the next measure actually re-runs, then re-runs it. */
  private refitAfterFontSwap(): void {
    if (this.destroyed) return;
    this.lastNameFingerprint = '';
    this.lastTextFingerprint = '';
    // Still null when the faces were already cached and resolved before the view existed;
    // the cleared fingerprints leave ngAfterViewChecked to do it with the right metrics.
    this.fitCardName();
    this.fitTextToBox();
  }

  onTouchStart(event: TouchEvent): void {
    if (event.touches.length !== 1 || !this.cardPreviewService.isPhoneLayout()) return;
    this.cancelLongPress();
    this.touchStartX = event.touches[0].clientX;
    this.touchStartY = event.touches[0].clientY;
    this.longPressTimer = setTimeout(() => {
      this.longPressTimer = null;
      this.longPressFired = true;
      this.cardPreviewService.show(this.card, this.permanent);
    }, CardDisplayComponent.LONG_PRESS_MS);
  }

  onTouchMove(event: TouchEvent): void {
    if (this.longPressTimer === null || event.touches.length !== 1) return;
    const dx = event.touches[0].clientX - this.touchStartX;
    const dy = event.touches[0].clientY - this.touchStartY;
    if (Math.hypot(dx, dy) > CardDisplayComponent.LONG_PRESS_SLOP_PX) {
      // The finger is scrolling, not holding; give up on the preview.
      this.clearLongPressTimer();
    }
  }

  onTouchEnd(event: TouchEvent): void {
    const fired = this.longPressFired;
    this.cancelLongPress();
    if (fired) {
      event.preventDefault();
    }
  }

  onTouchCancel(): void {
    this.cancelLongPress();
  }

  /** Android opens a context menu on long-press; swallow it while ours runs. */
  onContextMenu(event: Event): void {
    if (this.longPressTimer !== null || this.longPressFired) {
      event.preventDefault();
    }
  }

  private clearLongPressTimer(): void {
    if (this.longPressTimer !== null) {
      clearTimeout(this.longPressTimer);
      this.longPressTimer = null;
    }
  }

  private cancelLongPress(): void {
    this.clearLongPressTimer();
    if (this.longPressFired) {
      this.longPressFired = false;
      this.cardPreviewService.clear();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['card'] && !changes['card'].firstChange) || (changes['artFace'] && !changes['artFace'].firstChange)) {
      if (this.card.setCode && this.card.collectorNumber) {
        const cached = this.scryfallImageService.getCachedArtCropUrl(this.card.setCode, this.card.collectorNumber, this.artFace);
        if (cached) {
          this.artUrl.set(cached);
        } else {
          this.artUrl.set(null);
          this.fetchCardArt();
        }
      } else {
        this.artUrl.set(null);
      }
    }
  }

  /**
   * The frame's watermark, as the classes that draw it — `null` on the great majority of cards,
   * which have none, and also on the ones whose watermark the Mana font has no glyph for.
   *
   * <p>A plain getter, and it used to be a signal fed by a fetch. The watermark was an SVG pulled
   * off a GitHub raw URL and cached in IndexedDB, so it needed somewhere to land when it arrived
   * and a branch in ngOnChanges to re-fetch when the card changed underneath it. Now it is a
   * property of the card being rendered and nothing else.
   */
  get watermarkClasses(): string | null {
    const classes = watermarkSymbolClasses(this.card.watermark);
    return classes ? `watermark ${classes}` : null;
  }

  private fetchCardArt(): void {
    if (this.card.setCode && this.card.collectorNumber) {
      this.scryfallImageService.getArtCropUrl(this.card.setCode, this.card.collectorNumber, this.artFace)
        .then(url => this.artUrl.set(url))
        .catch(() => { this.artUrl.set(null); });
    }
  }

  private static readonly COLOR_CSS_MAP: Record<string, string> = {
    'BLACK': '#1a1a20',
    'GREEN': '#4a7c28',
    'BLUE': '#2c5ea2',
    'RED': '#a03030',
    'WHITE': '#f0e6b2',
  };

  /**
   * The same five colours in the earthy value range the tinted land frames use (these are
   * the midpoints of those ramps). A dual land is multicoloured by colour identity, so it
   * reaches multicolorBackground like any gold card would; blending the saturated spell
   * colours above would leave one shock land glowing beside a Mountain drawn in mud.
   */
  private static readonly LAND_COLOR_CSS_MAP: Record<string, string> = {
    'BLACK': '#31313a',
    'GREEN': '#3b5424',
    'BLUE': '#2f4d67',
    'RED': '#7d3a2c',
    'WHITE': '#bfa771',
  };

  @HostBinding('class.token-card')
  get isToken(): boolean {
    return this.card.token;
  }

  @HostBinding('attr.data-card-color')
  get cardColor(): string | null {
    const colors = this.card.colors;
    if (colors && colors.length > 1) {
      return 'MULTICOLOR';
    }
    return colors && colors.length === 1 ? colors[0] : this.card.color;
  }

  @HostBinding('style.background')
  get multicolorBackground(): string | null {
    const colors = this.card.colors;
    if (!colors || colors.length <= 1) {
      return null;
    }
    const palette = hasCardType(this.card, 'LAND')
      ? CardDisplayComponent.LAND_COLOR_CSS_MAP
      : CardDisplayComponent.COLOR_CSS_MAP;
    const cssColors = colors
      .map(c => palette[c])
      .filter((c): c is string => c != null);
    if (cssColors.length < 2) {
      return null;
    }
    if (cssColors.length === 2) {
      return `linear-gradient(135deg, ${cssColors[0]} 0%, ${cssColors[1]} 100%)`;
    }
    const stops = cssColors.map((c, i) =>
      `${c} ${Math.round((i / (cssColors.length - 1)) * 100)}%`
    );
    return `linear-gradient(135deg, ${stops.join(', ')})`;
  }

  /**
   * Frame treatment beyond the plain colour ramps — without it lands and colourless
   * artifacts share the default frame and cannot be told apart on the battlefield.
   *
   * Lands claim their frame before the colour check, and unconditionally: a land that
   * makes coloured mana still wants a land frame, so the CSS pairs this attribute with
   * data-card-color to *tint* the earthy frame rather than let the colour ramp replace
   * it. Artifact lands take the land frame too, which is how they are printed.
   *
   * A *coloured* artifact keeps its colour frame, as it does in print, so this returns
   * null once a non-land card has any colour. Everything else colourless — Eldrazi,
   * devoid cards — takes the pale colourless frame it is printed with, rather than
   * falling through to the default brown that also stands in for "no colour data".
   */
  @HostBinding('attr.data-frame')
  get frameStyle(): 'land' | 'artifact' | 'colorless' | null {
    if (hasCardType(this.card, 'LAND')) return 'land';
    if (this.cardColor) return null;
    return hasCardType(this.card, 'ARTIFACT') ? 'artifact' : 'colorless';
  }

  @HostBinding('class.legendary-card')
  get isLegendary(): boolean {
    return (this.card.supertypes ?? []).includes('LEGENDARY');
  }

  @HostBinding('class.is-tapped')
  get isTapped(): boolean {
    return !this.preview && !!this.permanent?.tapped;
  }

  get effectiveKeywords(): string[] {
    if (this.permanent && this.permanent.grantedKeywords) {
      return this.permanent.grantedKeywords.filter(kw => !this.card.keywords.includes(kw));
    }
    return [];
  }

  get grantedAbilityTexts(): string[] {
    return distinctGrantedAbilityTexts(this.permanent?.grantedAbilities ?? []);
  }

  get isBuffed(): boolean {
    return this.permanent != null &&
      (this.permanent.powerModifier > 0 || this.permanent.toughnessModifier > 0);
  }

  get isDebuffed(): boolean {
    return this.permanent != null &&
      (this.permanent.powerModifier < 0 || this.permanent.toughnessModifier < 0);
  }

  get isDamaged(): boolean {
    return this.permanent != null && this.permanent.markedDamage > 0;
  }

  /** Tokens draw their own P/T over the art and have no info line to clear. */
  @HostBinding('class.has-pt')
  get hasPowerToughness(): boolean {
    return !this.card.token
      && ((this.card.power != null && this.card.toughness != null) || !!this.permanent?.animatedCreature);
  }

  get displayPower(): number | null {
    if (this.permanent?.animatedCreature) return this.permanent.effectivePower;
    if (this.card.power == null) return null;
    return this.permanent ? this.permanent.effectivePower : this.card.power;
  }

  get displayToughness(): number | null {
    if (this.permanent?.animatedCreature) return this.permanent.effectiveToughness;
    if (this.card.toughness == null) return null;
    return this.permanent ? this.permanent.effectiveToughness : this.card.toughness;
  }

  /** Number of counters of the given type on this permanent (0 if none or no permanent). */
  counter(counterType: string): number {
    return this.permanent?.counters?.[counterType] ?? 0;
  }

  /** Counter types shown elsewhere on the card: loyalty has its own box, P/T counters are baked into effective P/T. */
  private static readonly BADGE_EXCLUDED_COUNTERS = new Set(['LOYALTY', 'PLUS_ONE_PLUS_ONE', 'MINUS_ONE_MINUS_ONE']);

  get badgeCounters(): { type: string; count: number }[] {
    if (this.preview || !this.permanent?.counters) return [];
    return Object.entries(this.permanent.counters)
      .filter(([type, count]) => count > 0 && !CardDisplayComponent.BADGE_EXCLUDED_COUNTERS.has(type))
      .map(([type, count]) => ({ type, count }));
  }

  get displayLoyalty(): number | null {
    if (this.permanent && this.counter('LOYALTY') > 0) return this.counter('LOYALTY');
    return this.card.loyalty ?? null;
  }

  get typeLine(): string {
    return formatTypeLine(this.card);
  }

  @HostBinding('class.planeswalker-card')
  get isPlaneswalker(): boolean {
    return hasCardType(this.card, 'PLANESWALKER');
  }

  /** Printed planeswalker frames only enlarge the ability box (type line riding
   *  up over the art) when there are four or more ability lines. */
  @HostBinding('class.pw-tall-box')
  get hasTallAbilityBox(): boolean {
    return this.isPlaneswalker && this.planeswalkerAbilities.length >= 4;
  }

  /** Loyalty costs in oracle text: "+1:", "0:", "−2:", "−X:" (Scryfall uses U+2212 minus). */
  private static readonly LOYALTY_COST_PATTERN = /^([+−–-]?)(\d+|X):\s*(.*)$/;

  get planeswalkerAbilities(): PlaneswalkerAbilityLine[] {
    if (!this.card.cardText) return [];
    return this.card.cardText.split('\n')
      .filter(line => line.trim().length > 0)
      .map(line => {
        const match = line.match(CardDisplayComponent.LOYALTY_COST_PATTERN);
        if (!match) {
          return { cost: null, dir: null, html: this.toSymbolHtml(line) };
        }
        const dir = match[1] === '+' ? 'up' as const : match[1] ? 'down' as const : 'zero' as const;
        const cost = (dir === 'up' ? '+' : dir === 'down' ? '−' : '') + match[2];
        return { cost, dir, html: this.toSymbolHtml(match[3]) };
      });
  }

  private toSymbolHtml(text: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(manaSymbolHtml(text));
  }

  get scryfallData() {
    if (!this.card.setCode || !this.card.collectorNumber) return null;
    return this.scryfallCardDataService.getCardData(this.card.setCode, this.card.collectorNumber);
  }

  get flavorText(): string | null {
    return this.scryfallData?.flavorText ?? null;
  }

  get artist(): string | null {
    return this.scryfallData?.artist ?? null;
  }

  get rarity(): string | null {
    return this.scryfallData?.rarity ?? null;
  }

  /** The prepare spell printed inset on a prepare card's front face, as projected by the server. */
  get prepareSpell(): Card | null {
    return this.card.prepareSpell ?? null;
  }

  get prepareSpellTypeLine(): string {
    return this.prepareSpell ? formatTypeLine(this.prepareSpell) : '';
  }

  /** Dimmed while the permanent is not currently prepared; on cards outside the battlefield
   *  there is no prepared state to reflect, so the inset stays fully lit. */
  get prepareSpellInactive(): boolean {
    return this.permanent != null && !this.permanent.prepared;
  }

  get prepareSpellManaCost(): SafeHtml {
    const cost = this.prepareSpell?.manaCost;
    if (!cost) return '';
    return this.sanitizer.bypassSecurityTrustHtml(manaSymbolHtml(cost));
  }

  get prepareSpellText(): SafeHtml {
    const text = this.prepareSpell?.cardText;
    if (!text) return '';
    return this.sanitizer.bypassSecurityTrustHtml(manaSymbolHtml(text));
  }

  ngAfterViewChecked(): void {
    this.fitCardName();
    this.fitTextToBox();
  }

  /**
   * Shrinks an over-long name until it fits its plate, so it is set smaller rather than
   * truncated ("Kozilek, the Great Distortion" instead of "Kozilek, the Great Dis…").
   * Measured on the name span itself, which flex has already sized against the mana cost
   * beside it — so the fingerprint tracks the symbol version too: the cost's width jumps
   * when its {G} placeholders are replaced by loaded symbol images.
   */
  private fitCardName(): void {
    const el = this.nameTextRef?.nativeElement;
    if (!el) return;

    /* Keyed on the rendered name plate rather than on the name, because the width the name has to
       fit into is what is left over after the mana cost beside it — so two cards with the same
       name and different costs are not the same fitting problem, and the plate is where that
       difference is visible. */
    const plate = el.parentElement;
    const fp = renderedTextKey({
      text: plate?.textContent ?? el.textContent ?? '',
      symbolCount: plate?.querySelectorAll('.mana-sym').length ?? 0,
    });
    if (fp === this.lastNameFingerprint) return;
    this.lastNameFingerprint = fp;

    // Same search as the rules text, measuring width instead of height. Bisection matters here
    // too: a name has one line to fit in, so the sizes a fixed step skips over are the whole
    // difference between "Antiquities on the Loose" and "Antiquities on the Lo…".
    largestFittingSize(
        CardDisplayComponent.MIN_NAME_FONT_SIZE,
        CardDisplayComponent.MAX_NAME_FONT_SIZE,
        CardDisplayComponent.FONT_PRECISION,
        size => {
          el.style.fontSize = size + 'px';
          return el.scrollWidth <= el.clientWidth;
        });
  }

  private fitTextToBox(): void {
    const el = this.textBoxRef?.nativeElement;
    if (!el) return;

    /* Keyed on what the box actually contains, not on what the card model says it should.
       Those two disagree for a whole change detection pass every time flavour text arrives, and a
       fit landing in that window used to store a key claiming flavour was present while measuring
       a box without it — after which the matching key suppressed the fit that would have corrected
       it, permanently. textContent covers rules text, keywords, granted abilities and the
       prepare-spell inset alike; the symbols are counted beside it because they are glyphs drawn
       by CSS and so appear in none of it. */
    const fp = renderedTextKey({
      text: el.textContent ?? '',
      symbolCount: el.querySelectorAll('.mana-sym').length,
    });
    if (fp === this.lastTextFingerprint) return;
    this.lastTextFingerprint = fp;

    const flavorEl = el.querySelector('.card-flavor-text') as HTMLElement | null;
    const separatorEl = el.querySelector('.flavor-separator') as HTMLElement | null;

    const minSize = this.isPlaneswalker
      ? CardDisplayComponent.PW_MIN_FONT_SIZE
      : CardDisplayComponent.MIN_FONT_SIZE;

    /* Rules text and flavour text share one size, the flavour a fixed fraction behind rather
       than a flat 2px: two pixels is a tenth off 20px type but a quarter off the 8px this box
       routinely lands on, which is where flavour stopped reading as smaller and started reading
       as broken. If nothing in range holds both, the flavour goes rather than being clipped —
       printed cards omit flavour text on wordy cards instead of setting rules text below
       legibility, and clipping is the one outcome print never accepts. */
    this.showFlavor(flavorEl, separatorEl, true);
    const applySize = (size: number) => {
      el.style.fontSize = size + 'px';
      if (flavorEl) {
        flavorEl.style.fontSize = (size * CardDisplayComponent.FLAVOR_FONT_RATIO) + 'px';
      }
    };
    let size = this.fitInto(el, minSize, applySize) ?? minSize;

    /* Flavour text is the first thing a printed card gives up: Wizards omits it outright on
       text-heavy cards rather than set the rules text below its legibility floor, which is why
       so many wordy commons have none. Shrinking to the floor and clipping whatever still hangs
       over the edge is the one outcome print never accepts, so when even the floor cannot hold
       both, the flavour goes and the rules text refits into the room it frees. */
    if (flavorEl && !this.contentFits(el)) {
      this.showFlavor(flavorEl, separatorEl, false);
      size = this.fitInto(el, minSize, s => { el.style.fontSize = s + 'px'; }) ?? minSize;
    }

    this.fitPrepareSpell(el, size);
  }

  /** Toggling display rather than a bound flag: this runs inside ngAfterViewChecked, where
   *  writing to a template binding would throw ExpressionChangedAfterItHasBeenChecked. */
  private showFlavor(flavorEl: HTMLElement | null, separatorEl: HTMLElement | null, shown: boolean): void {
    const display = shown ? '' : 'none';
    if (flavorEl) flavorEl.style.display = display;
    if (separatorEl) separatorEl.style.display = display;
  }

  private contentFits(el: HTMLElement): boolean {
    return el.scrollHeight <= el.clientHeight;
  }

  /** Binds the pure size search to this box's height. Null when even the floor overflows. */
  private fitInto(el: HTMLElement, minSize: number, apply: (size: number) => void): number | null {
    return largestFittingSize(
        minSize, CardDisplayComponent.MAX_FONT_SIZE, CardDisplayComponent.FONT_PRECISION,
        size => { apply(size); return this.contentFits(el); });
  }

  /**
   * Fits the prepare spell's inset within its own frame. Scaling the whole text box down
   * far enough to get five mana symbols across a ~60px column would leave the creature's
   * own rules text tiny for no reason, so the inset shrinks on its own instead — and would
   * otherwise spill out over the P/T plate below it.
   */
  private fitPrepareSpell(box: HTMLElement, boxFontSize: number): void {
    const panel = box.querySelector('.prepare-spell') as HTMLElement | null;
    const text = panel?.querySelector('.prepare-spell-text') as HTMLElement | null;
    if (!panel || !text) return;

    let size = boxFontSize * CardDisplayComponent.PREPARE_FONT_RATIO;
    panel.style.fontSize = size + 'px';

    while (size > CardDisplayComponent.PREPARE_MIN_FONT_SIZE
        && text.scrollHeight > text.clientHeight) {
      size -= CardDisplayComponent.FONT_STEP;
      panel.style.fontSize = size + 'px';
    }
  }

  get setSymbolUrl(): string | null {
    if (!this.card.setCode) return null;
    return this.setSymbolService.getSymbolUrl(this.card.setCode);
  }

  /** The symbol as a CSS url() token: it is painted as a mask over a flat rarity
   *  colour rather than as an <img>, so it reaches CSS as a custom property. */
  get setSymbolCssUrl(): string | null {
    const url = this.setSymbolUrl;
    return url ? `url("${url}")` : null;
  }

  get formattedManaCost(): SafeHtml {
    if (!this.card.manaCost) return '';
    return this.sanitizer.bypassSecurityTrustHtml(manaSymbolHtml(this.card.manaCost));
  }

  get formattedCardText(): SafeHtml {
    if (!this.card.cardText) return '';
    return this.sanitizer.bypassSecurityTrustHtml(manaSymbolHtml(this.card.cardText));
  }

}
