import { Injectable, inject, signal } from '@angular/core';
import { ScryfallFetchQueue } from './scryfall-fetch-queue.service';

const DB_NAME = 'mana-symbols-cache';
const STORE_NAME = 'symbols';
const DB_VERSION = 1;

@Injectable({ providedIn: 'root' })
export class ManaSymbolService {

  private symbolUrls = new Map<string, string>();
  private dbPromise: Promise<IDBDatabase>;
  private inFlight = new Map<string, Promise<string>>();
  private fetchQueue = inject(ScryfallFetchQueue);

  /** Increments as symbols load — read in templates/computed to trigger re-renders */
  symbolsVersion = signal(0);

  private static PRELOAD = [
    'W', 'U', 'B', 'R', 'G', 'C', 'X', 'T',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10'
  ];

  constructor() {
    this.dbPromise = this.openDb();
    this.preload();
  }

  getSymbolUrl(symbol: string): string | null {
    return this.symbolUrls.get(symbol) ?? null;
  }

  /**
   * Replace {X} patterns in text with inline <img> tags.
   * Unloaded symbols remain as text and are queued for loading.
   * Reads symbolsVersion signal so Angular can track reactivity.
   */
  replaceSymbols(text: string): string {
    this.symbolsVersion();
    return text.replace(/\{([^}]+)\}/g, (match, sym: string) => {
      const url = this.symbolUrls.get(sym);
      if (url) {
        return `<img class="mana-sym" src="${url}" alt="${match}" style="height:1em;vertical-align:middle;margin:0 1px;">`;
      }
      this.ensureLoaded(sym);
      return match;
    });
  }

  private ensureLoaded(symbol: string): void {
    if (this.symbolUrls.has(symbol) || this.inFlight.has(symbol)) return;
    this.loadSymbol(symbol);
  }

  private loadSymbol(symbol: string): Promise<string> {
    const existing = this.inFlight.get(symbol);
    if (existing) return existing;

    const cached = this.symbolUrls.get(symbol);
    if (cached) return Promise.resolve(cached);

    const cacheKey = symbol;
    const url = `https://svgs.scryfall.io/card-symbols/${encodeURIComponent(symbol)}.svg`;

    const promise = this.getFromDb(cacheKey).then(blob => {
      if (blob) {
        return this.toObjectUrl(cacheKey, blob);
      }
      return this.fetchQueue.enqueue(url).then(fetchedBlob => {
        this.putInDb(cacheKey, fetchedBlob).catch(() => {});
        return this.toObjectUrl(cacheKey, fetchedBlob);
      });
    });

    this.inFlight.set(symbol, promise);
    promise
      .then(() => this.symbolsVersion.update(v => v + 1))
      .catch(() => {})
      .finally(() => this.inFlight.delete(symbol));

    return promise;
  }

  private async preload(): Promise<void> {
    for (const sym of ManaSymbolService.PRELOAD) {
      try {
        await this.loadSymbol(sym);
      } catch { /* ignore failed symbols */ }
    }
  }

  private openDb(): Promise<IDBDatabase> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION);
      request.onupgradeneeded = () => {
        request.result.createObjectStore(STORE_NAME);
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  private async getFromDb(key: string): Promise<Blob | undefined> {
    const db = await this.dbPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readonly');
      const req = tx.objectStore(STORE_NAME).get(key);
      req.onsuccess = () => resolve(req.result ?? undefined);
      req.onerror = () => reject(req.error);
    });
  }

  private async putInDb(key: string, blob: Blob): Promise<void> {
    const db = await this.dbPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readwrite');
      const req = tx.objectStore(STORE_NAME).put(blob, key);
      req.onsuccess = () => resolve();
      req.onerror = () => reject(req.error);
    });
  }

  private toObjectUrl(cacheKey: string, blob: Blob): string {
    const objectUrl = URL.createObjectURL(blob);
    this.symbolUrls.set(cacheKey, objectUrl);
    return objectUrl;
  }
}
