package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToHandEffect;

import java.util.List;

public class GiantSecrets extends Card {

    private static final List<ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference> SPELLBOOK = List.of(
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("M19", "43"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "2"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("ZEN", "6"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("DOM", "49"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("ISD", "49"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("RTR", "9"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("MH1", "8"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("RTR", "13"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("M19", "60"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("DOM", "28"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("M21", "67"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("THB", "228"),
            new ConjureRandomCardFromSpellbookToHandEffect.CardPrintingReference("BRO", "65"));

    public GiantSecrets() {
        addEffect(EffectSlot.SPELL,
                new ConjureRandomCardFromSpellbookToHandEffect(SPELLBOOK, new XValue()));
    }
}
