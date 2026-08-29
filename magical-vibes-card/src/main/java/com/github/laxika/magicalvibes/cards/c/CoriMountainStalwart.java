package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "175")
public class CoriMountainStalwart extends Card {

    public CoriMountainStalwart() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2)
                )
        ));
    }
}
