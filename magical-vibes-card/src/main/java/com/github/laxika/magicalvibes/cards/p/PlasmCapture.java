package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaEqualToTargetSpellManaValueEffect;

@CardRegistration(set = "DGM", collectorNumber = "91")
public class PlasmCapture extends Card {

    public PlasmCapture() {
        // Counter target spell. At the beginning of your next first main phase, add X mana in any
        // combination of colors, where X is that spell's mana value.
        //
        // The delayed-mana registration is listed before the counter so the targeted spell is still
        // on the stack when its mana value is snapshotted; the two instructions are independent, so
        // this ordering is rules-equivalent.
        addEffect(EffectSlot.SPELL, new RegisterDelayedManaEqualToTargetSpellManaValueEffect(
                ManaColor.COLORLESS, false, true, true));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
