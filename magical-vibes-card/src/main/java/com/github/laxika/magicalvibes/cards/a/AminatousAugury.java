package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastOnePerCardTypeEffect;

@CardRegistration(set = "CMM", collectorNumber = "73")
@CardRegistration(set = "CMM", collectorNumber = "479")
public class AminatousAugury extends Card {

    public AminatousAugury() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsAndMayCastOnePerCardTypeEffect(8));
    }
}
