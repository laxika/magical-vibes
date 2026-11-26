package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "17")
public class ColdSnap extends Card {

    public ColdSnap() {
        // Cumulative upkeep {2}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{2}"));

        // At the beginning of each player's upkeep, deal damage to that player equal to
        // the number of snow lands they control. Triggers.upkeep(ActivePlayer.ANY) sets the active player as target.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                DealDamageEffect.to(Subjects.triggeringPlayer(), new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentHasSupertypePredicate(CardSupertype.SNOW))),
                                CountScope.TARGET_PLAYER)));
    }
}
