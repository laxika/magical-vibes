package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ME1", collectorNumber = "112")
@CardRegistration(set = "ARN", collectorNumber = "44")
public class YdwenEfreet extends Card {

    public YdwenEfreet() {
        // Whenever this creature blocks, flip a coin. If you lose the flip, remove this creature from combat and it
        // can't block this turn. Creatures it was blocking that had become blocked by only this creature this combat
        // become unblocked.
        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                new FlipCoinWinEffect(null, SequenceEffect.of(
                        new RemoveTargetFromCombatEffect(Subjects.source(), true),
                        new CantBlockThisTurnEffect(Subjects.source()))));
    }
}
