package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnAndBoostSelfIfMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "213")
public class DaredevilManWithoutFear extends Card {

    public DaredevilManWithoutFear() {
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addTrigger(Triggers.youAttack(), new MayEffect(
                new ExileTopCardMayPlayThisTurnAndBoostSelfIfMatchingEffect(
                        new CardSubtypePredicate(CardSubtype.HERO), 2, 1),
                "Exile the top card of your library?"));
    }
}
