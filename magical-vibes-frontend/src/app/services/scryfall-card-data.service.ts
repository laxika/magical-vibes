import { Injectable, inject, signal } from '@angular/core';
import { ScryfallFetchQueue } from './scryfall-fetch-queue.service';

export interface ScryfallCardData {
  flavorText: string | null;
  artist: string;
  rarity: string; // "COMMON" | "UNCOMMON" | "RARE" | "MYTHIC"
}

interface ScryfallSearchResponse {
  data: ScryfallCardEntry[];
  has_more: boolean;
  next_page?: string;
}

interface ScryfallCardEntry {
  collector_number: string;
  flavor_text?: string;
  artist: string;
  rarity: string;
}

const DB_NAME = 'scryfall-card-data-cache';
const STORE_NAME = 'set-data';
const DB_VERSION = 1;

@Injectable({ providedIn: 'root' })
export class ScryfallCardDataService {

  private cache = new Map<string, ScryfallCardData>();
  private setStatus = new Map<string, 'loading' | 'loaded' | 'failed'>();
  private inFlightSets = new Map<string, Promise<void>>();
  private dbPromise: Promise<IDBDatabase>;
  private fetchQueue = inject(ScryfallFetchQueue);

  /** Increments as card data loads — read in templates/computed to trigger re-renders */
  dataVersion = signal(0);

  constructor() {
    this.dbPromise = this.openDb();
  }

  getCardData(setCode: string, collectorNumber: string): ScryfallCardData | null {
    this.dataVersion();
    const key = `${setCode}:${collectorNumber}`;
    const cached = this.cache.get(key);
    if (cached) return cached;
    this.ensureSetLoaded(setCode);
    return null;
  }

  private ensureSetLoaded(setCode: string): void {
    const lowerSet = setCode.toLowerCase();
    const status = this.setStatus.get(lowerSet);
    if (status) return;
    this.setStatus.set(lowerSet, 'loading');
    this.loadSet(lowerSet, setCode);
  }

  private loadSet(lowerSet: string, originalSetCode: string): void {
    if (this.inFlightSets.has(lowerSet)) return;

    const promise = this.loadFromDb(lowerSet).then(dbData => {
      if (dbData) {
        this.populateCache(originalSetCode, dbData);
        this.setStatus.set(lowerSet, 'loaded');
        this.dataVersion.update(v => v + 1);
        return;
      }
      return this.fetchSet(lowerSet, originalSetCode);
    }).catch(() => {
      this.setStatus.set(lowerSet, 'failed');
    }).finally(() => {
      this.inFlightSets.delete(lowerSet);
    });

    this.inFlightSets.set(lowerSet, promise);
  }

  private async fetchSet(lowerSet: string, originalSetCode: string): Promise<void> {
    const allCards: Record<string, ScryfallCardData> = {};

    let url: string | null = `https://api.scryfall.com/cards/search?q=set:${encodeURIComponent(lowerSet)}&unique=prints`;

    while (url) {
      const page: ScryfallSearchResponse = await this.fetchQueue.enqueueJson<ScryfallSearchResponse>(url);
      for (const card of page.data) {
        allCards[card.collector_number] = {
          flavorText: card.flavor_text ?? null,
          artist: card.artist,
          rarity: card.rarity.toUpperCase()
        };
      }
      url = page.has_more && page.next_page ? page.next_page : null;
    }

    this.populateCache(originalSetCode, allCards);
    this.putInDb(lowerSet, allCards).catch(() => {});
    this.setStatus.set(lowerSet, 'loaded');
    this.dataVersion.update(v => v + 1);
  }

  private populateCache(setCode: string, data: Record<string, ScryfallCardData>): void {
    for (const [collectorNumber, cardData] of Object.entries(data)) {
      this.cache.set(`${setCode}:${collectorNumber}`, cardData);
    }
  }

  // IndexedDB

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

  private async loadFromDb(setKey: string): Promise<Record<string, ScryfallCardData> | undefined> {
    const db = await this.dbPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readonly');
      const req = tx.objectStore(STORE_NAME).get(setKey);
      req.onsuccess = () => resolve(req.result ?? undefined);
      req.onerror = () => reject(req.error);
    });
  }

  private async putInDb(setKey: string, data: Record<string, ScryfallCardData>): Promise<void> {
    const db = await this.dbPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction(STORE_NAME, 'readwrite');
      const req = tx.objectStore(STORE_NAME).put(data, setKey);
      req.onsuccess = () => resolve();
      req.onerror = () => reject(req.error);
    });
  }
}
