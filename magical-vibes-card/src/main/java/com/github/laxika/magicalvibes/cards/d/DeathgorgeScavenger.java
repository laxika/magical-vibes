package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "XLN", collectorNumber = "184")
public class DeathgorgeScavenger extends Card {

    public DeathgorgeScavenger() {
        // Whenever Deathgorge Scavenger enters or attacks, you may exile target card from a
        // graveyard. If a creature card is exiled this way, you gain 2 life. If a noncreature card
        // is exiled this way, Deathgorge Scavenger gets +1/+1 until end of turn.
        MayEffect mayExile = new MayEffect(
                new ExileGraveyardCardWithConditionalBonusEffect(2, 1, 1),
                "Exile target card from a graveyard?"
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, mayExile);
        addEffect(EffectSlot.ON_ATTACK, mayExile);
    }
}
