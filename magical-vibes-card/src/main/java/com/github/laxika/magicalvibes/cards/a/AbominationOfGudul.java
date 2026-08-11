package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "KTK", collectorNumber = "159")
public class AbominationOfGudul extends Card {

    public AbominationOfGudul() {
        addMorph("{2}{B}{G}{U}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                        "You may draw a card. If you do, discard a card."));
    }
}
