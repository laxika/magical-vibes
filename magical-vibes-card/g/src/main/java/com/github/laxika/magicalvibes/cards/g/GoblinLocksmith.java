package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "104")
public class GoblinLocksmith extends Card {

    public GoblinLocksmith() {
        // Whenever this creature attacks, creatures with defender can't block this turn.
        addTrigger(Triggers.attacks(), new CantBlockThisTurnEffect(
                TapUntapScope.ALL_CREATURES, new PermanentHasKeywordPredicate(Keyword.DEFENDER)));
    }
}
