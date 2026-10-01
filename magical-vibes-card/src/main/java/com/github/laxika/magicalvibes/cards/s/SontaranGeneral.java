package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "96")
@CardRegistration(set = "WHO", collectorNumber = "701")
public class SontaranGeneral extends Card {

    public SontaranGeneral() {
        // Battalion — Whenever this creature and at least two other creatures attack,
        // for each opponent, goad up to one target creature that player controls.
        // Those creatures can't block this turn.
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creatureAnOpponentControls(), 0, 99)
                .addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                        new MinimumAttackers(3),
                        SequenceEffect.of(
                                new GoadTargetCreatureUntilNextTurnEffect(),
                                new CantBlockThisTurnEffect(TapUntapScope.TARGET))));
    }
}
