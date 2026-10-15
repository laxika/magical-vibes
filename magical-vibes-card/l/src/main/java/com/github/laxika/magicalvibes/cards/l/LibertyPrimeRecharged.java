package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "5")
@CardRegistration(set = "PIP", collectorNumber = "416")
@CardRegistration(set = "PIP", collectorNumber = "533")
@CardRegistration(set = "PIP", collectorNumber = "944")
public class LibertyPrimeRecharged extends Card {

    public LibertyPrimeRecharged() {
        var sacrificeUnlessPayEnergy = new ForcedCostOrElseEffect(
                new PayEnergyCost(2), List.of(new SacrificeSelfEffect()), true);
        addEffect(EffectSlot.ON_ATTACK, sacrificeUnlessPayEnergy);
        addEffect(EffectSlot.ON_BLOCK, sacrificeUnlessPayEnergy);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "an artifact", false),
                        new EnergyCountersEffect(2),
                        new DrawCardEffect(1)
                ),
                "{2}, {T}, Sacrifice an artifact: You get {E}{E} and draw a card."
        ));
    }
}
