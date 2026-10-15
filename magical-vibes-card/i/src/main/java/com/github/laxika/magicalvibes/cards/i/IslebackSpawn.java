package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyLibraryAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SHM", collectorNumber = "40")
public class IslebackSpawn extends Card {

    public IslebackSpawn() {
        // Shroud is auto-loaded from Scryfall keywords.
        // This creature gets +4/+8 as long as a library has twenty or fewer cards in it.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new AnyLibraryAtMost(20), new StaticBoostEffect(4, 8, GrantScope.SELF)));
    }
}
