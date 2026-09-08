package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "204")
public class SenateGuildmage extends Card {

    public SenateGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new GainLifeEffect(2)),
                "{W}, {T}: You gain 2 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{U}, {T}: Draw a card, then discard a card."
        ));
    }
}
