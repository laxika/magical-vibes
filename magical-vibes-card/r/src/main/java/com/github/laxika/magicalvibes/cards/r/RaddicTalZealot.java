package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToHandEffect;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "22")
public class RaddicTalZealot extends Card {

    private static final List<DraftCardFromSpellbookToHandEffect.CardPrintingReference> SPELLBOOK = List.of(
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("GRN", "77"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("AFR", "18"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("M20", "105"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("M20", "10"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("M20", "94"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("DOM", "6"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "97"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "99"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "1"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("DOM", "14"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("M19", "42"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "105"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "79"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("DOM", "21"),
            new DraftCardFromSpellbookToHandEffect.CardPrintingReference("ELD", "9"));

    public RaddicTalZealot() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new MinimumAttackingCreaturesOfSubtype(1, CardSubtype.KNIGHT),
                new DraftCardFromSpellbookToHandEffect(SPELLBOOK)));
    }
}
