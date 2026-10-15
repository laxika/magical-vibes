package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.amount.GreatestStackSourceCountThisTurn;

@CardRegistration(set = "MB1", collectorNumber = "69")
public class GeometricWeird extends Card {

    public GeometricWeird() {
        GreatestStackSourceCountThisTurn stackSourceCount = new GreatestStackSourceCountThisTurn();
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new MayEffect(
                        new SetBasePowerToughnessToAmountEffect(
                                stackSourceCount, stackSourceCount, GrantScope.SELF),
                        "Have Geometric Weird's base power and toughness become equal to the greatest number "
                                + "of spells and abilities from different sources that were on the stack "
                                + "simultaneously this turn?"));
    }
}
