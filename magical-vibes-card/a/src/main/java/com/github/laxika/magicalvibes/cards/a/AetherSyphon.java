package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "38")
public class AetherSyphon extends Card {

    public AetherSyphon() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new ConditionalEffect(
                new MaxSpeed(), new MillEffect(2, MillRecipient.EACH_OPPONENT)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DrawCardEffect(1)),
                "{2}, {T}: Draw a card."
        ));
    }
}
