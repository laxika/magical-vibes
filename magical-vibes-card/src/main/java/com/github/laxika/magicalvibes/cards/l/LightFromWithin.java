package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesByManaSymbolEffect;

@CardRegistration(set = "EVE", collectorNumber = "10")
public class LightFromWithin extends Card {

    public LightFromWithin() {
        // Chroma — Each creature you control gets +1/+1 for each white mana symbol in its mana cost.
        addEffect(EffectSlot.STATIC, new BoostOwnCreaturesByManaSymbolEffect(ManaColor.WHITE, 1, 1));
    }
}
