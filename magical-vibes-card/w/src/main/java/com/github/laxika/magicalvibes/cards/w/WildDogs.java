package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMostLifePredicate;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.APlayerHasMoreLifeThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "284")
@CardRegistration(set = "DMR", collectorNumber = "183")
public class WildDogs extends Card {

    public WildDogs() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new APlayerHasMoreLifeThanEachOtherPlayer()),
                GainControlOfTargetEffect.of(Subjects.source(), Duration.PERMANENT)
                        .gainedBy(Subjects.matching(Subjects.eachPlayer(), new PlayerHasMostLifePredicate())));
        addCycling("{2}");
    }
}
