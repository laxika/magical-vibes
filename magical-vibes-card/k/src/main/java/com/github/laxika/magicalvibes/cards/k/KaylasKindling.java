package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "9")
public class KaylasKindling extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("HOU", "83"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "137"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "164"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "140"),
            new DraftFromSpellbookEffect.SpellbookCard("RTR", "98"),
            new DraftFromSpellbookEffect.SpellbookCard("MH1", "145"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "164"),
            new DraftFromSpellbookEffect.SpellbookCard("CON", "58"),
            new DraftFromSpellbookEffect.SpellbookCard("LEA", "161"),
            new DraftFromSpellbookEffect.SpellbookCard("C14", "34"),
            new DraftFromSpellbookEffect.SpellbookCard("RNA", "99"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "95"),
            new DraftFromSpellbookEffect.SpellbookCard("CON", "74"),
            new DraftFromSpellbookEffect.SpellbookCard("M14", "163"),
            new DraftFromSpellbookEffect.SpellbookCard("SCG", "103"));

    public KaylasKindling() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DraftCardFromSpellbookEffect(SPELLBOOK, true));
    }
}
