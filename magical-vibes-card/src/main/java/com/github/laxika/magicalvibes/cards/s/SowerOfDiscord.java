package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.OtherChosenPlayerLosesLifeEffect;

@CardRegistration(set = "CMM", collectorNumber = "187")
@CardRegistration(set = "CMM", collectorNumber = "522")
public class SowerOfDiscord extends Card {

    public SowerOfDiscord() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChoosePlayerOnEnterEffect(2));
        addEffect(EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, new OtherChosenPlayerLosesLifeEffect());
    }
}
