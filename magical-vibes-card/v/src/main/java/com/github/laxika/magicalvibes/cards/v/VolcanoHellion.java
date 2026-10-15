package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.ChooseAnyNumberEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "111")
public class VolcanoHellion extends Card {

    public VolcanoHellion() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseAnyNumberEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new DealDamageToTargetCreatureEffect(new ChosenNumberOnSource(), true));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToPlayersEffect(new ChosenNumberOnSource(), DamageRecipient.CONTROLLER, true));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RegisterEchoAtNextUpkeepEffect(new ControllerLifeTotal()));
    }
}
