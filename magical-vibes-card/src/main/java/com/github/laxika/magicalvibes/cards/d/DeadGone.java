package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "113")
public class DeadGone extends Card {

    public DeadGone() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Dead — Deal 2 damage to target creature",
                        new DealDamageToTargetCreatureEffect(2),
                        TargetFilters.creature()
                ).withManaCost("{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Gone — Return target creature you don't control to its owner's hand",
                        ReturnToHandEffect.target(),
                        TargetFilters.creatureAnOpponentControls()
                ).withManaCost("{2}{R}")
        )));
    }
}
