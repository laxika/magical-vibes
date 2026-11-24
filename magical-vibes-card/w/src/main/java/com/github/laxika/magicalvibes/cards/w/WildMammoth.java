package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.PlayerControlsMostCreaturesPredicate;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.APlayerControlsMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "124")
public class WildMammoth extends Card {

    public WildMammoth() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new APlayerControlsMoreCreaturesThanEachOtherPlayer()),
                GainControlOfTargetEffect.of(Subjects.source(), Duration.PERMANENT)
                        .gainedBy(Subjects.matching(Subjects.eachPlayer(), new PlayerControlsMostCreaturesPredicate())));
    }
}
