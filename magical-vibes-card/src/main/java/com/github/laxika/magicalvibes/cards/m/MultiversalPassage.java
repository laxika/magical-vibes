package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenBasicLandTypeEffect;

@CardRegistration(set = "SPM", collectorNumber = "180")
public class MultiversalPassage extends Card {

    public MultiversalPassage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseBasicLandTypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new MayPayLifeOrEntersTappedEffect(2));
        addEffect(EffectSlot.STATIC, new SourceBecomesChosenBasicLandTypeEffect());
    }
}
