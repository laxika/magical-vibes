import { describe, expect, it } from 'vitest';

import { distinctGrantedAbilityTexts, formatKeywords } from './format-utils';

describe('formatKeywords', () => {
  it('formats every separator in a multi-word keyword', () => {
    expect(formatKeywords(['HEXPROOF_FROM_BLACK'])).toBe('Hexproof from black');
  });
});

describe('distinctGrantedAbilityTexts', () => {
  it('shows one card-text line when multiple sources grant the same ability', () => {
    expect(distinctGrantedAbilityTexts([
      { text: 'Protection from red' },
      { text: 'Protection from red' },
      { text: "Can't be blocked" },
    ])).toEqual(['Protection from red', "Can't be blocked"]);
  });
});
