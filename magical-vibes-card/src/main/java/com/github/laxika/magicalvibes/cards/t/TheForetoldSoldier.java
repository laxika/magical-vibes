package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndBecomeForetoldEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

@CardRegistration(set = "WHO", collectorNumber = "102")
@CardRegistration(set = "WHO", collectorNumber = "395")
@CardRegistration(set = "WHO", collectorNumber = "707")
@CardRegistration(set = "WHO", collectorNumber = "986")
public class TheForetoldSoldier extends Card {

    public TheForetoldSoldier() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());
        addEffect(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1));
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new ExileSelfAndBecomeForetoldEffect());
        addCastingOption(new ForetellCast("{1}{G}"));
    }
}
