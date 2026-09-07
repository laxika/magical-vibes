package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "55")
public class EpharasDispersal extends Card {

    public EpharasDispersal() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingPermanentEffect(
                new PermanentIsAttackingPredicate(), 2));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addEffect(EffectSlot.SPELL, new SurveilEffect(2));
    }
}
