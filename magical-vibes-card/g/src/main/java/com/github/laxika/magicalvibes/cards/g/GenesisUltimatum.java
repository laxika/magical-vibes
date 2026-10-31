package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "IKO", collectorNumber = "189")
@CardRegistration(set = "SPG", collectorNumber = "106")
@CardRegistration(set = "MSC", collectorNumber = "283")
@CardRegistration(set = "MSC", collectorNumber = "402")
public class GenesisUltimatum extends Card {

    public GenesisUltimatum() {
        addSpellEffect(LookAtTopCardsEffect.putAnyNumberMatchingOntoBattlefieldRestToHand(
                        5, new CardIsPermanentPredicate()));
        addSpellEffect(new ExileSpellEffect());
    }
}
