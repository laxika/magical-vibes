package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "64")
public class MnemonicSphere extends Card {

    public MnemonicSphere() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{1}{U}, Sacrifice this artifact: Draw two cards."
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new DrawCardEffect()),
                "Channel — {U}, Discard this card: Draw a card."
        ));
    }
}
