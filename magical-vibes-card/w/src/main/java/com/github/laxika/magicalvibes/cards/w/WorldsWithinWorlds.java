package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.WorldsWithinWorldsEffect;

@CardRegistration(set = "MSH", collectorNumber = "241")
public class WorldsWithinWorlds extends Card {

    public WorldsWithinWorlds() {
        addSpellEffect(new WorldsWithinWorldsEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
