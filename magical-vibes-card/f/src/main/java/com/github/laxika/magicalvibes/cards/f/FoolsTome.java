package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "289")
public class FoolsTome extends Card {

    public FoolsTome() {
        // {2}, {T}: Draw a card. Activate only if you have no cards in hand.
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(new DrawCardEffect(1)),
                "{2}, {T}: Draw a card. Activate only if you have no cards in hand.")
                .withActivationCondition(new ControllerHandEmpty(),
                        "Activate only if you have no cards in hand"));
    }
}
