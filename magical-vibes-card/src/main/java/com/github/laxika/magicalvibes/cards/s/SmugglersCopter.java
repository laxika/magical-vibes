package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "235")
public class SmugglersCopter extends Card {

    public SmugglersCopter() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "Draw a card, then discard a card?"
        ));
        addEffect(EffectSlot.ON_BLOCK, new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "Draw a card, then discard a card?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                "Crew 1"
        ));
    }
}
