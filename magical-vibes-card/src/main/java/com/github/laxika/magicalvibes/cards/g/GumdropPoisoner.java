package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TemptWithTreats;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "93")
public class GumdropPoisoner extends Card {

    public GumdropPoisoner() {
        setBackFaceCard(new TemptWithTreats());
        addCastingOption(new AdventureCast("{B}"));

        var minusLifeGained = new Scaled(new LifeGainedThisTurn(CountScope.CONTROLLER), -1);
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new BoostTargetCreatureEffect(minusLifeGained, minusLifeGained));
    }

    @Override
    public String getBackFaceClassName() {
        return "TemptWithTreats";
    }
}
