package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithAPermanentPredicate;

@CardRegistration(set = "MBS", collectorNumber = "27")
public class MitoticManipulation extends Card {

    public MitoticManipulation() {
        addSpellEffect(LookAtTopCardsEffect.mayPutMatchingOntoBattlefield(
                7, new CardSharesNameWithAPermanentPredicate()));
    }
}
