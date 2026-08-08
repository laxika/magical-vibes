package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerCantCastSpellsThisTurnEffect;

@CardRegistration(set = "DGM", collectorNumber = "96")
public class RenderSilent extends Card {

    public RenderSilent() {
        // Counter target spell. Its controller can't cast spells this turn.
        //
        // The cast restriction is listed before the counter so the targeted spell is still on the
        // stack when its controller is resolved.
        addEffect(EffectSlot.SPELL, new TargetSpellControllerCantCastSpellsThisTurnEffect());
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
