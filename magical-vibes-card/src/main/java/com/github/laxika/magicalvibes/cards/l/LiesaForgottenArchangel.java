package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;

@CardRegistration(set = "INR", collectorNumber = "243")
@CardRegistration(set = "INR", collectorNumber = "433")
public class LiesaForgottenArchangel extends Card {

    public LiesaForgottenArchangel() {
        // Whenever another nontoken creature you control dies, return that card to its owner's hand
        // at the beginning of the next end step. The collector binds the dying card's id.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));

        // If a creature an opponent controls would die, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileOpponentCreaturesInsteadOfDyingEffect());
    }
}
