package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect;

@CardRegistration(set = "VOW", collectorNumber = "191")
public class CemeteryProwler extends Card {

    public CemeteryProwler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileAnyGraveyardCardAndImprintOnSourceEffect());
        addEffect(EffectSlot.ON_ATTACK, new ExileAnyGraveyardCardAndImprintOnSourceEffect());
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect());
    }
}
