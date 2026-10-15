package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "158")
public class ReinforcedRonin extends Card {

    public ReinforcedRonin() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, ReturnToHandEffect.self());

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DrawCardEffect()),
                "Channel — {1}{R}, Discard this card: Draw a card."
        ));
    }
}
