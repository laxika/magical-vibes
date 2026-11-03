package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "10")
@CardRegistration(set = "OPCA", collectorNumber = "24")
public class FieldsOfSummer extends Card {

    public FieldsOfSummer() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new MayEffect(new GainLifeEffect(new Fixed(2), GainLifeRecipient.TRIGGERING_PLAYER),
                        "Gain 2 life?", null, MayChoicePlayer.TRIGGERING_SPELL_CONTROLLER));
        addTrigger(Triggers.chaosEnsues(), new MayEffect(new GainLifeEffect(10), "Gain 10 life?"));
    }
}
