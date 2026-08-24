package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SPM", collectorNumber = "83")
public class MaximumCarnage extends Card {

    public MaximumCarnage() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new GoadCreaturesUntilNextTurnEffect(new PermanentTruePredicate()));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new AwardManaEffect(ManaColor.RED, 3));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new DealDamageToPlayersEffect(5, DamageRecipient.EACH_OPPONENT));
    }
}
