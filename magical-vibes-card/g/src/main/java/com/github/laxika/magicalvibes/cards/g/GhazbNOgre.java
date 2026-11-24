package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "5ED", collectorNumber = "298")
@CardRegistration(set = "CHR", collectorNumber = "65")
@CardRegistration(set = "ME1", collectorNumber = "120")
@CardRegistration(set = "ARN", collectorNumber = "49")
public class GhazbNOgre extends Card {

    public GhazbNOgre() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new APlayerHasMoreLifeThanEachOtherPlayer()),
                GainControlOfTargetEffect.of(Subjects.source(), Duration.PERMANENT)
                        .gainedBy(Subjects.matching(Subjects.eachPlayer(), new PlayerHasMostLifePredicate())));
    }
}
