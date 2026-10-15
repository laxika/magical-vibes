package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "56")
public class RavingVisionary extends Card {

    public RavingVisionary() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{U}, {T}: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(new DrawCardEffect()),
                "{2}{U}, {T}: Draw a card. Activate only if there are four or more card types among cards in your graveyard."
        ).withActivationCondition(
                new Delirium(),
                "Activate only if there are four or more card types among cards in your graveyard."
        ));
    }
}
