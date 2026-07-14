package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDrawsCardEffect;

@CardRegistration(set = "EVE", collectorNumber = "19")
public class DreamFracture extends Card {

    public DreamFracture() {
        // Counter target spell. Its controller draws a card. Draw a card.
        //
        // The "its controller draws" is listed before the counter so the targeted spell is still on
        // the stack, letting TargetSpellControllerDrawsCardEffect resolve its controller. The
        // instructions are independent (the draw does not depend on whether the spell was countered),
        // so this ordering is rules-equivalent and also handles uncounterable spells correctly.
        addEffect(EffectSlot.SPELL, new TargetSpellControllerDrawsCardEffect());
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
