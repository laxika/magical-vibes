package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfEnlistedNontokenCreatureEffect;

@CardRegistration(set = "YDMU", collectorNumber = "14")
public class GoblinMoraleSergeant extends Card {

    public GoblinMoraleSergeant() {
        // Haste and Enlist are loaded from Scryfall; the enlist continuation is handled by combat.
        addEffect(EffectSlot.STATIC, new ConjureDuplicateOfEnlistedNontokenCreatureEffect());
    }
}
