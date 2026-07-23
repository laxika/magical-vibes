package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BasicLandsOfChosenTypesBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

@CardRegistration(set = "ICE", collectorNumber = "77")
public class IllusionaryTerrain extends Card {

    public IllusionaryTerrain() {
        // Cumulative upkeep {2}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{2}"));

        // As this enchantment enters, choose two basic land types.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseBasicLandTypeOnEnterEffect(2));

        // Basic lands of the first chosen type are the second chosen type.
        addEffect(EffectSlot.STATIC, new BasicLandsOfChosenTypesBecomeTypeEffect());
    }
}
