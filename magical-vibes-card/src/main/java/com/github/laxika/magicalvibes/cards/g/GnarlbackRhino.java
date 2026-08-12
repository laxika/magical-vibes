package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "300")
public class GnarlbackRhino extends Card {

    public GnarlbackRhino() {
        // Whenever you cast a spell that targets this creature, draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect(1)),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
