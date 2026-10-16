package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

/**
 * Double strike and prowess come from the Scryfall-loaded keywords; prowess is applied by the engine's
 * keyword-driven spell-cast trigger, so no per-card prowess effect is added here (it would pump twice).
 */
@CardRegistration(set = "TDM", collectorNumber = "195")
public class JeskaiBrushmaster extends Card {
}
