package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyTwoColorsPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

public class GuildpactParagon extends Card {

    public GuildpactParagon() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardHasExactlyTwoColorsPredicate(),
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        6, new CardHasExactlyTwoColorsPredicate()))
        ));
    }
}
