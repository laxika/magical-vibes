package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;

@CardRegistration(set = "ALA", collectorNumber = "183")
public class PunishIgnorance extends Card {

    public PunishIgnorance() {
        // Counter target spell. Its controller loses 3 life and you gain 3 life.
        //
        // The controller life loss is listed before the counter so the targeted spell is still on
        // the stack, letting TargetSpellControllerLosesLifeEffect resolve its controller. Targeting
        // is auto-derived for CounterSpellEffect.
        addEffect(EffectSlot.SPELL, new TargetSpellControllerLosesLifeEffect(3));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
