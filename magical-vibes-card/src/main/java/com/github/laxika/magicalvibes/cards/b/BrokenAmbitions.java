package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "LRW", collectorNumber = "54")
public class BrokenAmbitions extends Card {

    public BrokenAmbitions() {
        // Counter target spell unless its controller pays {X}. Clash with an opponent. If you win,
        // that spell's controller mills four cards.
        //
        // The clash is listed before the counter so that on a clash win the targeted spell is still
        // on the stack, letting MillRecipient.TARGET_SPELL_CONTROLLER resolve its controller. The two
        // instructions are independent (the mill does not depend on whether the spell was countered),
        // so this ordering is rules-equivalent.
        addEffect(EffectSlot.SPELL,
                new ClashEffect(new MillEffect(4, MillRecipient.TARGET_SPELL_CONTROLLER)));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, false));
    }
}
