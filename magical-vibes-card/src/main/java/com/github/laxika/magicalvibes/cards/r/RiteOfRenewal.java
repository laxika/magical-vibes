package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "TDM", collectorNumber = "153")
public class RiteOfRenewal extends Card {

    public RiteOfRenewal() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardIsPermanentPredicate(), 2));
        addEffect(EffectSlot.SPELL, new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 4));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
