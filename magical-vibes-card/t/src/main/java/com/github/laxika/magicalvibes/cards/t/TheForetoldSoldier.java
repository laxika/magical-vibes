package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndBecomeForetoldEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "102")
@CardRegistration(set = "WHO", collectorNumber = "395")
@CardRegistration(set = "WHO", collectorNumber = "707")
@CardRegistration(set = "WHO", collectorNumber = "986")
public class TheForetoldSoldier extends Card {

    public TheForetoldSoldier() {
        addStaticEffect(new MustBeBlockedIfAbleEffect());
        addStaticEffect(new CanBeBlockedByAtMostNCreaturesEffect(1));
        addTrigger(Triggers.dealsDamage(), new ExileSelfAndBecomeForetoldEffect());
        addCastingOption(new ForetellCast("{1}{G}"));
    }
}
