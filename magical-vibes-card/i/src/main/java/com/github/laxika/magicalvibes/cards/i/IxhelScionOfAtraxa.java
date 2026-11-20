package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerHasPoisonCountersAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONC", collectorNumber = "1")
@CardRegistration(set = "ONC", collectorNumber = "29")
@CardRegistration(set = "ONC", collectorNumber = "37")
public class IxhelScionOfAtraxa extends Card {

    public IxhelScionOfAtraxa() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ExileTopCardMayPlayThisTurnEffect(false)
                        .fromLibraryOf(Subjects.matching(Subjects.eachOpponent(),
                                new PlayerHasPoisonCountersAtLeastPredicate(3)))
                        .exiledFaceDown().withAnyManaType().until(Duration.PERMANENT));
    }
}
