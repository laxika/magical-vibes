package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;

@CardRegistration(set = "7ED", collectorNumber = "133")
public class EngineeredPlague extends Card {

    public EngineeredPlague() {
        // As this enchantment enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // All creatures of the chosen type get -1/-1 (all controllers).
        addEffect(EffectSlot.STATIC, new BoostCreaturesOfChosenSubtypeEffect(-1, -1, null, true));
    }
}
