package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceEquipCostEffect;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "35")
public class ArmsScavenger extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "133"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "243"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "223"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "140"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "250"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "155"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "239"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "221"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "151"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "234"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "248"),
            new DraftFromSpellbookEffect.SpellbookCard("M15", "227"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "157"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "228"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "254"));

    public ArmsScavenger() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DraftCardFromSpellbookEffect(SPELLBOOK, true));
        addStaticEffect(new ReduceEquipCostEffect(1));
    }
}
