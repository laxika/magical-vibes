package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TimeVaultReplacementEffect;

import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "287")
public class TimeVault extends Card {

    public TimeVault() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
        addEffect(EffectSlot.STATIC, new TimeVaultReplacementEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ControllerExtraTurnEffect(1)),
                "{T}: Take an extra turn after this one."
        ));
    }
}
