package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "ARB", collectorNumber = "81")
public class IdentityCrisis extends Card {

    public IdentityCrisis() {
        // Exile all cards from target player's hand and graveyard.
        // Both effects share the single player target (see EffectResolution.computeAllowedTargets).
        addEffect(EffectSlot.SPELL, new ExileTargetPlayerHandEffect());
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE));
    }
}
