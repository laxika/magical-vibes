package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "93")
public class RMSTitanic extends Card {

    public RMSTitanic() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        CreateTokenEffect.ofTreasureToken(new EventValue())));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }
}
