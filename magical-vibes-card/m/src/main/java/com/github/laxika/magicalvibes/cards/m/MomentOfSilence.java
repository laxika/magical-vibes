package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MMQ", collectorNumber = "28")
public class MomentOfSilence extends Card {

    public MomentOfSilence() {
        // Target player skips their next combat phase this turn.
        addSpellEffect(new SkipNextEffect(SkipKind.COMBAT_PHASE, Subjects.targetPlayer(), false, true));
    }
}
