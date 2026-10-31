package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "APC", collectorNumber = "53")
@CardRegistration(set = "EMA", collectorNumber = "111")
@CardRegistration(set = "VMA", collectorNumber = "144")
@CardRegistration(set = "DMR", collectorNumber = "107")
public class UrborgUprising extends Card {

    public UrborgUprising() {
        addSpellEffect(new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), 2));
        addSpellEffect(new DrawCardEffect());
    }
}
