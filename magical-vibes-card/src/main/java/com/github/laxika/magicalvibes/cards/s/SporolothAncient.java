package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "152")
public class SporolothAncient extends Card {

    public SporolothAncient() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.FUNGUS));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        null,
                        List.of(
                                new RemoveCounterFromSourceCost(2, CounterType.FUNGUS),
                                new CreateTokenEffect(
                                        "Saproling",
                                        1,
                                        1,
                                        CardColor.GREEN,
                                        List.of(CardSubtype.SAPROLING),
                                        Set.of(),
                                        Set.of()
                                )
                        ),
                        "Remove two spore counters from this creature: Create a 1/1 green Saproling creature token."
                ),
                GrantScope.ALL_OWN_CREATURES
        ));
    }
}
