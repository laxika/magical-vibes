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
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new TargetSpellControllerLosesLifeEffect(3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
