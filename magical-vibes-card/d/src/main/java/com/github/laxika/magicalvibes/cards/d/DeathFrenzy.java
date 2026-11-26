package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "172")
public class DeathFrenzy extends Card {

    public DeathFrenzy() {
        addSpellEffect(BoostEffect.allCreatures(-2, -2));
        addSpellEffect(new RegisterGlobalTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), new GainLifeEffect(1))));
    }
}
