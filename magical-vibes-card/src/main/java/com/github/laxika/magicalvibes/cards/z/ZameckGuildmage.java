package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "209")
public class ZameckGuildmage extends Card {

    public ZameckGuildmage() {
        // {G}{U}: This turn, each creature you control enters with an additional +1/+1 counter on it.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{U}",
                List.of(new ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect(1)),
                "{G}{U}: This turn, each creature you control enters with an additional +1/+1 counter on it."
        ));

        // {G}{U}, Remove a +1/+1 counter from a creature you control: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{U}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)
                ),
                "{G}{U}, Remove a +1/+1 counter from a creature you control: Draw a card."
        ));
    }
}
