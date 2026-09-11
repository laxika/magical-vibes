package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "255")
public class ReckonerBankbuster extends Card {

    public ReckonerBankbuster() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new DrawCardEffect(),
                        new ConditionalEffect(
                                new NotCondition(new SourceCounterThreshold(1, CounterType.CHARGE)),
                                SequenceEffect.of(CreateTokenEffect.ofTreasureToken(1), pilotToken()))
                ),
                "{2}, {T}, Remove a charge counter from Reckoner Bankbuster: Draw a card. Then if there are no charge counters on it, create a Treasure token and a 1/1 colorless Pilot creature token with \"This token crews Vehicles as though its power were 2 greater.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }

    private static CreateTokenEffect pilotToken() {
        return new CreateTokenEffect(
                1,
                "Pilot",
                1,
                1,
                null,
                List.of(CardSubtype.PILOT),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2))
        );
    }
}
