package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "215")
public class ThunderousSnapper extends Card {

    public ThunderousSnapper() {
        // Whenever you cast a spell with mana value 5 or greater, draw a card.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect()),
                new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(4))));
    }
}
