package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;

@CardRegistration(set = "EXO", collectorNumber = "125")
public class SongOfSerenity extends Card {

    public SongOfSerenity() {
        // Creatures that are enchanted can't attack or block.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantAttackOrBlockEffect(
                new PermanentIsEnchantedPredicate(),
                "Creatures that are enchanted can't attack or block"));
    }
}
