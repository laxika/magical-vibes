package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScavengeEqualToManaCostToCreatureCardsEffect;

@CardRegistration(set = "PIP", collectorNumber = "125")
@CardRegistration(set = "PIP", collectorNumber = "653")
public class YoungDeathclaws extends Card {

    public YoungDeathclaws() {
        // Each creature card in your graveyard has scavenge. The scavenge cost is equal to its mana cost.
        addEffect(EffectSlot.STATIC, new GrantScavengeEqualToManaCostToCreatureCardsEffect());
    }
}
