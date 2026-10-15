package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "HBG", collectorNumber = "110")
public class AncientSilverDragon extends Card {

    public AncientSilverDragon() {
        // Whenever this creature deals combat damage to a player, roll a d20. Draw cards equal to
        // the result. You have no maximum hand size for the rest of the game.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                SequenceEffect.of(new RollD20Effect(
                        new DrawCardEffect(new EventValue()),
                        new DrawCardEffect(new EventValue()),
                        new DrawCardEffect(new EventValue())),
                new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.REST_OF_GAME)));
    }
}
