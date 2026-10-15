package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCounterPTEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "223")
public class GrakmawSkyclaveRavager extends Card {

    public GrakmawSkyclaveRavager() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));

        CreateTokenEffect hydraToken = new CreateTokenEffect(
                "Hydra", 0, 0, CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.GREEN), List.of(CardSubtype.HYDRA));
        addEffect(EffectSlot.ON_DEATH,
                new CreateTokenWithDyingSourceCounterPTEffect(hydraToken, CounterType.PLUS_ONE_PLUS_ONE));
    }
}
