package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;

/**
 * Meddle targets any spell — the "only one target and that target is a creature" clause is an
 * intervening condition checked on resolution, not a targeting restriction.
 */
@CardRegistration(set = "MIR", collectorNumber = "73")
public class Meddle extends Card {

    public Meddle() {
        addEffect(EffectSlot.SPELL, new ChangeTargetOfTargetSpellWithSingleTargetEffect(true));
    }
}
