package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1294")
public class GandalfFriendOfTheShire extends Card {

    public GandalfFriendOfTheShire() {
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.ON_CONTROLLER_TEMPTS_RING, new DrawCardEffect());
    }
}
