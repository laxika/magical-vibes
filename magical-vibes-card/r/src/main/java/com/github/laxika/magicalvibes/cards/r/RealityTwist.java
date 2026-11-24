package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.TwistBasicLandManaColorsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Map;

@CardRegistration(set = "ICE", collectorNumber = "94")
public class RealityTwist extends Card {

    public RealityTwist() {
        // Cumulative upkeep {1}{U}{U}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}{U}{U}"));

        // If tapped for mana, Plains produce {R}, Swamps produce {G}, Mountains produce {W},
        // and Forests produce {B} instead of any other type.
        addStaticEffect(new TwistBasicLandManaColorsEffect(Map.of(
                CardSubtype.PLAINS, ManaColor.RED,
                CardSubtype.SWAMP, ManaColor.GREEN,
                CardSubtype.MOUNTAIN, ManaColor.WHITE,
                CardSubtype.FOREST, ManaColor.BLACK)));
    }
}
