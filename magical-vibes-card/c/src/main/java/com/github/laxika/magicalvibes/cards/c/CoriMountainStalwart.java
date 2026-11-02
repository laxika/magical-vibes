package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "175")
public class CoriMountainStalwart extends Card {

    public CoriMountainStalwart() {
        addTrigger(Triggers.youCastSpell(), new NthSpellCastTriggerEffect(
                2,
                List.of(
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2)
                )
        ));
    }
}
