package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyTwoColorsPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


public class GuildpactParagon extends Card {

    public GuildpactParagon() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasExactlyTwoColorsPredicate()),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(6, new CardHasExactlyTwoColorsPredicate()));
    }
}
