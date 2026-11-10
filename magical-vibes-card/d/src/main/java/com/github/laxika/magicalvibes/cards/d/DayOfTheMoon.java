package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenNamePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "79")
@CardRegistration(set = "WHO", collectorNumber = "684")
public class DayOfTheMoon extends Card {

    public DayOfTheMoon() {
        for (int chapter : new int[]{1, 2, 3}) {
            addTrigger(Triggers.chapter(chapter),
                    new ChooseCardNameAtResolutionEffect(CardType.CREATURE),
                    new GoadCreaturesUntilNextTurnSnapshotEffect(
                            new PermanentHasSourceChosenNamePredicate(true)));
        }
    }
}
