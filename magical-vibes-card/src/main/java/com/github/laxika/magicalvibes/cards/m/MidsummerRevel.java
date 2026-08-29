package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "268")
public class MidsummerRevel extends Card {

    public MidsummerRevel() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.VERSE),
                "Put a verse counter on Midsummer Revel?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new CreateTokenEffect(
                                new CountersOnSource(CounterType.VERSE),
                                "Beast", 3, 3, CardColor.GREEN,
                                List.of(CardSubtype.BEAST), Set.of(), Set.of())
                ),
                "{G}, Sacrifice this enchantment: Create X 3/3 green Beast creature tokens, where X is the number of verse counters on this enchantment."
        ));
    }
}
