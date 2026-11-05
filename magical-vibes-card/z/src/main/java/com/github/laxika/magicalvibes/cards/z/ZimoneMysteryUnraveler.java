package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TurnOwnCreatureFaceUpEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "8")
public class ZimoneMysteryUnraveler extends Card {

    public ZimoneMysteryUnraveler() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new ConditionalEffect(
                new NthAbilityResolutionThisTurn(1), ManifestDreadEffect.forController()),
                new ConditionalEffect(
                new NotCondition(new NthAbilityResolutionThisTurn(1)),
                new MayEffect(TurnOwnCreatureFaceUpEffect.anyPermanent(), "Turn a permanent you control face up?")));
    }
}
