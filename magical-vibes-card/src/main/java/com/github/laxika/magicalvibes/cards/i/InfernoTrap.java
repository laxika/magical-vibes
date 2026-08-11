package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerDealtDamageByAtLeastCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "133")
public class InfernoTrap extends Card {

    public InfernoTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{R}")),
                new ControllerDealtDamageByAtLeastCreaturesThisTurn(2),
                false));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
    }
}
