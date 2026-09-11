package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GraveyardsAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;

@CardRegistration(set = "HOB", collectorNumber = "77")
public class TheMasterOfLakeTown extends Card {

    public TheMasterOfLakeTown() {
        MillOpponentOnLifeLossEffect millOnLifeLoss = new MillOpponentOnLifeLossEffect();
        addEffect(EffectSlot.ON_OPPONENT_LOSES_LIFE, millOnLifeLoss);
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE, millOnLifeLoss);
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(new GraveyardsAtLeast(7)));
    }
}
