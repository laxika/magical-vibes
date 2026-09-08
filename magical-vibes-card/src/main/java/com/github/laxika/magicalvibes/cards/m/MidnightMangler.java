package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "50")
public class MidnightMangler extends Card {

    public MidnightMangler() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new NotControllerTurn(),
                new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null, Set.of(),
                        GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN, null)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
