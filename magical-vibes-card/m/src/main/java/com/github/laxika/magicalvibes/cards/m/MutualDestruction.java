package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "96")
public class MutualDestruction extends Card {

    public MutualDestruction() {
        // This spell has flash as long as you control a permanent with flash.
        setFlashCastCondition(new ControlsPermanent(new PermanentHasKeywordPredicate(Keyword.FLASH)));

        // As an additional cost to cast this spell, sacrifice a creature.
        // Destroy target creature.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new SacrificeCreatureCost())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
