package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "153")
public class Putrefaction extends Card {

    public Putrefaction() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.GREEN),
                                new CardColorPredicate(CardColor.WHITE))),
                        List.of(new DiscardEffect(1, DiscardRecipient.TRIGGERING_PLAYER, false))));
    }
}
