package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyTwoColorsPredicate;

import java.util.List;

public class GuildpactParagon extends Card {

    public GuildpactParagon() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardHasExactlyTwoColorsPredicate(),
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        6, new CardHasExactlyTwoColorsPredicate()))
        ));
    }
}
