package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChoosePlaneswalkerTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHexproofToChosenPlaneswalkerTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PreserveOneLoyaltyCounterForChosenPlaneswalkerTypeEffect;

@CardRegistration(set = "MAT", collectorNumber = "2")
@CardRegistration(set = "MAT", collectorNumber = "52")
@CardRegistration(set = "MAT", collectorNumber = "102")
@CardRegistration(set = "MAT", collectorNumber = "151")
@CardRegistration(set = "MAT", collectorNumber = "187")
public class Deification extends Card {

    public Deification() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChoosePlaneswalkerTypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new GrantHexproofToChosenPlaneswalkerTypeEffect());
        addEffect(EffectSlot.STATIC, new PreserveOneLoyaltyCounterForChosenPlaneswalkerTypeEffect());
    }
}
