package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "39")
public class GhostLitWarder extends Card {

    public GhostLitWarder() {
        addActivatedAbility(new ActivatedAbility(true, "{3}{U}",
                List.of(new CounterUnlessPaysEffect(2)),
                "{3}{U}, {T}: Counter target spell unless its controller pays {2}."));

        addHandActivatedAbility(new ActivatedAbility(false, "{3}{U}",
                List.of(new CounterUnlessPaysEffect(4)),
                "Channel — {3}{U}, Discard this card: Counter target spell unless its controller pays {4}."));
    }
}
