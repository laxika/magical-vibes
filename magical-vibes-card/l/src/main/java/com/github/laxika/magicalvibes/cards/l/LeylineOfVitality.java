package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "183")
public class LeylineOfVitality extends Card {

    public LeylineOfVitality() {
        addStaticEffect(new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of Vitality on the battlefield?"
        ));
        addStaticEffect(new StaticBoostEffect(0, 1, GrantScope.ALL_OWN_CREATURES));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL), new MayEffect(
                new GainLifeEffect(1),
                "Gain 1 life?"
        ));
    }
}
