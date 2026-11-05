package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OwnEffectsCantAffectSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "86")
public class AGoodThing extends Card {

    public AGoodThing() {
        addStaticEffect(new OwnEffectsCantAffectSourceEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new DoubleControllerLifeEffect(),
                new ConditionalEffect(new ControllerLifeAtLeast(1_000), new ControllerLosesGameEffect())));
    }
}
