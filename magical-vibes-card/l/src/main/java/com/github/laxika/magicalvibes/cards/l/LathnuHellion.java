package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "121")
public class LathnuHellion extends Card {

    public LathnuHellion() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EnergyCountersEffect(2));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay(
                        new PayEnergyCost(2),
                        new SacrificeSelfEffect()));
    }
}
