package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "USG", collectorNumber = "138")
public class IllGottenGains extends Card {

    public IllGottenGains() {
        addSpellEffect(ExileSpellEffect.immediately());
        addSpellEffect(new DiscardHandEffect(Subjects.eachPlayer()));
        addSpellEffect(new EachPlayerReturnsCardsFromGraveyardToHandEffect(
                3, new CardTruePredicate()));
    }
}
