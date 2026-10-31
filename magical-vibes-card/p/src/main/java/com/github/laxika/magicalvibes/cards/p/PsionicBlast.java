package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "TSB", collectorNumber = "30")
@CardRegistration(set = "2ED", collectorNumber = "75")
@CardRegistration(set = "LEA", collectorNumber = "74")
@CardRegistration(set = "LEB", collectorNumber = "75")
public class PsionicBlast extends Card {

    public PsionicBlast() {
        addSimultaneousSpellEffects(new DealDamageToAnyTargetEffect(4),
                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
    }
}
