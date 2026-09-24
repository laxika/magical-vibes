package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "NEO", collectorNumber = "250")
public class MirrorBox extends Card {

    public MirrorBox() {
        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleForControlledPermanentsEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new BoostByOtherCreaturesWithSameNameEffect(1, 1, true),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    }
}
