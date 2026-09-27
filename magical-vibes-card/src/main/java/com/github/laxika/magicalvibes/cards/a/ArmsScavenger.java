package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceEquipCostEffect;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "35")
public class ArmsScavenger extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Boots of Speed",
            "Cliffhaven Kitesail",
            "Colossus Hammer",
            "Dueling Rapier",
            "Spare Dagger",
            "Tormentor's Helm",
            "Goldvein Pick",
            "Jousting Lance",
            "Mask of Immolation",
            "Mirror Shield",
            "Relic Axe",
            "Rogue's Gloves",
            "Scavenged Blade",
            "Shield of the Realm",
            "Ceremonial Knife");

    public ArmsScavenger() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DraftCardFromSpellbookEffect(SPELLBOOK, true));
        addEffect(EffectSlot.STATIC, new ReduceEquipCostEffect(1));
    }
}
