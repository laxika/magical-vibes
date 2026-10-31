package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "RAV", collectorNumber = "117")
@CardRegistration(set = "V13", collectorNumber = "14")
@CardRegistration(set = "GK1", collectorNumber = "28")
public class Char extends Card {

    public Char() {
        addSimultaneousSpellEffects(new DealDamageToAnyTargetEffect(4),
                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
    }
}
