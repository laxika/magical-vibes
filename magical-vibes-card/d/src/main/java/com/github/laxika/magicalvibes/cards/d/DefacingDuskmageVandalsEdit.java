package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VandalsEdit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;

@CardRegistration(set = "SOC", collectorNumber = "45")
@CardRegistration(set = "SOC", collectorNumber = "93")
public class DefacingDuskmageVandalsEdit extends Card {

    public DefacingDuskmageVandalsEdit() {
        setBackFaceCard(new VandalsEdit());

        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new NthCardDrawTriggerEffect(2, new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "VandalsEdit";
    }
}
