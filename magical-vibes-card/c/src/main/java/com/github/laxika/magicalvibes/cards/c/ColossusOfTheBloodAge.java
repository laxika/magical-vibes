package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SOS", collectorNumber = "181")
public class ColossusOfTheBloodAge extends Card {

    public ColossusOfTheBloodAge() {
        // When this creature enters, it deals 3 damage to each opponent and you gain 3 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));

        // When this creature dies, discard any number of cards, then draw that many cards plus one.
        addEffect(EffectSlot.ON_DEATH, new DiscardUpToThenDrawThatManyEffect(
                DiscardUpToThenDrawThatManyEffect.ANY_NUMBER, 1));
    }
}
