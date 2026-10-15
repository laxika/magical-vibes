package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CraftWithPride;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Goblin Glasswright // Craft with Pride (SOS 117).
 * <p>
 * Goblin Glasswright enters prepared, creating a copy of Craft with Pride in exile that its
 * controller may cast. Casting that copy unprepares Goblin Glasswright.
 */
@CardRegistration(set = "SOS", collectorNumber = "117")
public class GoblinGlasswrightCraftWithPride extends Card {

    public GoblinGlasswrightCraftWithPride() {
        setBackFaceCard(new CraftWithPride());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "CraftWithPride";
    }
}
