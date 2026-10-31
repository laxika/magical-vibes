package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "USG", collectorNumber = "138")
public class IllGottenGains extends Card {

    public IllGottenGains() {
        addSpellEffect(ExileSpellEffect.immediately());
        addSpellEffect(new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));
        addSpellEffect(new EachPlayerReturnsCardsFromGraveyardToHandEffect(
                3, new CardTruePredicate()));
    }
}
