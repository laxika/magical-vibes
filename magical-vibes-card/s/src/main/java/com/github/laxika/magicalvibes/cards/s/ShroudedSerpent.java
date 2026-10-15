package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

@CardRegistration(set = "PCY", collectorNumber = "47")
public class ShroudedSerpent extends Card {

    public ShroudedSerpent() {
        // Whenever this creature attacks, defending player may pay {4}. If that player doesn't,
        // this creature can't be blocked this turn.
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{4}",
                null,
                "Pay {4} to allow this creature to be blocked this turn?",
                MayPayPayer.DEFENDING_PLAYER,
                new MakeCreatureUnblockableEffect(true),
                0
        ));
    }
}
