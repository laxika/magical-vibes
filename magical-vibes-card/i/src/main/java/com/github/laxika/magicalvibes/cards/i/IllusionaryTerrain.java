package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BasicLandsOfChosenTypesBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "77")
public class IllusionaryTerrain extends Card {

    public IllusionaryTerrain() {
        // Cumulative upkeep {2}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{2}"));

        // As this enchantment enters, choose two basic land types.
        addStaticEffect(new ChooseBasicLandTypeOnEnterEffect(2));

        // Basic lands of the first chosen type are the second chosen type.
        addStaticEffect(new BasicLandsOfChosenTypesBecomeTypeEffect());
    }
}
