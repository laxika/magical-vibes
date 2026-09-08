package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "IKO", collectorNumber = "189")
public class GenesisUltimatum extends Card {

    public GenesisUltimatum() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.putAnyNumberMatchingOntoBattlefieldRestToHand(
                        5, new CardIsPermanentPredicate()));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
