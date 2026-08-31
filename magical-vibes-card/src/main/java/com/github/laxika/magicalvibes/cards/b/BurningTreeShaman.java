package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "GPT", collectorNumber = "105")
public class BurningTreeShaman extends Card {

    public BurningTreeShaman() {
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY,
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER));
    }
}
