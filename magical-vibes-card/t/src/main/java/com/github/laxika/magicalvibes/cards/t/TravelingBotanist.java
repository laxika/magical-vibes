package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "164")
public class TravelingBotanist extends Card {

    public TravelingBotanist() {
        addTrigger(Triggers.becomesTapped(),
                new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardTypePredicate(CardType.LAND), true));
    }
}
