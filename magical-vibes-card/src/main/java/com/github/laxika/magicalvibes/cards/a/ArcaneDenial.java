package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "ALL", collectorNumber = "22a")
@CardRegistration(set = "ALL", collectorNumber = "22b")
public class ArcaneDenial extends Card {

    public ArcaneDenial() {
        // Counter target spell. Its controller may draw up to two cards at the beginning of the next
        // turn's upkeep.
        // You draw a card at the beginning of the next turn's upkeep.
        //
        // The countered spell's controller registration is listed before the counter so the targeted
        // spell is still on the stack when its controller is read (as in Dream Fracture). Both
        // instructions are independent of whether the spell was actually countered, so this ordering
        // is rules-equivalent and also handles uncounterable spells.
        addEffect(EffectSlot.SPELL, RegisterDrawCardsAtNextUpkeepEffect.targetSpellControllerMayDrawUpTo(2));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
