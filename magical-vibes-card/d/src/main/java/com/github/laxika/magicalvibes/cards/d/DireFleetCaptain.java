package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "221")
public class DireFleetCaptain extends Card {

    public DireFleetCaptain() {
        // Whenever Dire Fleet Captain attacks, it gets +1/+1 until end of turn
        // for each other attacking Pirate.
        PermanentCount otherAttackingPirates = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.PIRATE)
                )),
                CountScope.CONTROLLER,
                true);
        addTrigger(Triggers.attacks(), new BoostSelfEffect(otherAttackingPirates, otherAttackingPirates));
    }
}
