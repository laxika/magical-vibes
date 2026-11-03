package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

public class AvatarYangchen extends Card {

    public AvatarYangchen() {
        PermanentPredicate anotherNonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        PermanentPredicateTargetFilter airbendTarget = new PermanentPredicateTargetFilter(
                anotherNonland, "Target must be another nonland permanent");
        target(airbendTarget, 0, 1)
                .addTrigger(Triggers.youCastSpell().nthThisTurn(2), new AirbendTargetPermanentEffect());
    }
}
