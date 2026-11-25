package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SHM", collectorNumber = "76")
public class RiteOfConsumption extends Card {

    public RiteOfConsumption() {
        // As an additional cost to cast this spell, sacrifice a creature; snapshots its power into xValue.
        addCastingCost(new SacrificeCreatureCost(false, true));

        // Deals damage equal to the sacrificed creature's power to target player or planeswalker, and you
        // gain life equal to the damage actually dealt this way.
        addSpellEffect(DealDamageEffect.toTargetPlayerOrPlaneswalker(new XValue()));
        addSpellEffect(new GainLifeEffect(new AmountThisWay(StepKind.DEAL_DAMAGE), Subjects.you()));
    }
}
