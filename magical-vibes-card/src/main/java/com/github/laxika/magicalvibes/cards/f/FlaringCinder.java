package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "225")
public class FlaringCinder extends Card {

    public FlaringCinder() {
        // When this creature enters and whenever you cast a spell with mana value 4 or greater,
        // you may discard a card. If you do, draw a card.
        MayEffect rummage = new MayEffect(
                new DiscardAndDrawCardEffect(),
                "Discard a card to draw a card?"
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, rummage);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardMinManaValuePredicate(4),
                List.of(rummage)
        ));
    }
}
