package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TurnOwnCreatureFaceUpEffect;

@CardRegistration(set = "DSC", collectorNumber = "8")
public class ZimoneMysteryUnraveler extends Card {

    public ZimoneMysteryUnraveler() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, ConditionalEffect.unless(
                new NthAbilityResolutionThisTurn(1), ManifestDreadEffect.forController()));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, ConditionalEffect.unless(
                new NotCondition(new NthAbilityResolutionThisTurn(1)),
                new MayEffect(new TurnOwnCreatureFaceUpEffect(), "Turn a permanent you control face up?")));
    }
}
