package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1294")
public class GandalfFriendOfTheShire extends Card {

    public GandalfFriendOfTheShire() {
        addStaticEffect(new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.ringTemptsYouAndYouChooseRingBearer(), new DrawCardEffect());
    }
}
