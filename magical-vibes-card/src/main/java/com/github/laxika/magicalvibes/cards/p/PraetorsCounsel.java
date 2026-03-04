package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPermanentNoMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "MBS", collectorNumber = "88")
public class PraetorsCounsel extends Card {

    public PraetorsCounsel() {
        // Return all cards from your graveyard to your hand.
        addEffect(EffectSlot.SPELL, new ReturnCardFromGraveyardEffect(
                GraveyardChoiceDestination.HAND,
                null,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                false,
                true,   // returnAll
                false,
                null,
                false
        ));
        // Exile Praetor's Counsel. (handled by StackResolutionService as spell disposition)
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
        // You have no maximum hand size for the rest of the game.
        addEffect(EffectSlot.SPELL, new GrantPermanentNoMaxHandSizeEffect());
    }
}
