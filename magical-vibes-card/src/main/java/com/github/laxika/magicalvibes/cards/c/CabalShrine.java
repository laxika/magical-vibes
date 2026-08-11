package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardForSameNameCardsInGraveyardsOnSpellCastEffect;

@CardRegistration(set = "ODY", collectorNumber = "121")
public class CabalShrine extends Card {

    public CabalShrine() {
        // Whenever a player casts a spell, that player discards X cards, where X is the number of
        // cards in all graveyards with the same name as that spell.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new DiscardForSameNameCardsInGraveyardsOnSpellCastEffect());
    }
}
