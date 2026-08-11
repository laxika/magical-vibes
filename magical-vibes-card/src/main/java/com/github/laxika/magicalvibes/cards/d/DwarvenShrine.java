package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect;

@CardRegistration(set = "ODY", collectorNumber = "187")
public class DwarvenShrine extends Card {

    public DwarvenShrine() {
        // Whenever a player casts a spell, this enchantment deals twice the number of matching
        // cards in all graveyards in damage to that player.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new DealDamageForSameNameCardsInGraveyardsOnSpellCastEffect());
    }
}
