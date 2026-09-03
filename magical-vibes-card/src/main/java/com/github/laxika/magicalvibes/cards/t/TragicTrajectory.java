package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "122")
public class TragicTrajectory extends Card {

    public TragicTrajectory() {
        // Target creature gets -2/-2 until end of turn.
        // Void — That creature gets -10/-10 until end of turn instead if a nonland permanent left
        // the battlefield this turn or a spell was warped this turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new VoidCondition(),
                new BoostTargetCreatureEffect(-2, -2),
                new BoostTargetCreatureEffect(-10, -10)));
    }
}
