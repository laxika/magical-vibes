package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "278")
public class Reparations extends Card {

    public Reparations() {
        // Whenever an opponent casts a spell that targets you or a creature you control, you may draw a card.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new MayEffect(
                        new SpellCastTriggerEffect(
                                null,
                                List.of(new DrawCardEffect()),
                                new StackEntryTargetsYouOrCreatureYouControlPredicate()
                        ),
                        "Draw a card?"
                ));
    }
}
