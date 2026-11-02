package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "143")
@CardRegistration(set = "INR", collectorNumber = "395")
@CardRegistration(set = "EMN", collectorNumber = "119")
@CardRegistration(set = "SIR", collectorNumber = "144")
public class BloodMist extends Card {

    public BloodMist() {
        // At the beginning of combat on your turn, target creature you control gains double strike
        // until end of turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be a creature you control"
        ))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
    }
}
