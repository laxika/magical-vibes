package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "107")
@CardRegistration(set = "M19", collectorNumber = "108")
@CardRegistration(set = "FDN", collectorNumber = "177")
@CardRegistration(set = "SOI", collectorNumber = "121")
@CardRegistration(set = "SIR", collectorNumber = "121")
@CardRegistration(set = "DIS", collectorNumber = "47")
@CardRegistration(set = "RVR", collectorNumber = "82")
public class MacabreWaltz extends Card {

    public MacabreWaltz() {
        addSpellEffect(new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), 2));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER, false));
    }
}
