package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "84")
public class SlitheryStalker extends Card {

    private static final PermanentAllOfPredicate TARGET = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE)),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
    ));

    public SlitheryStalker() {
        target(new PermanentPredicateTargetFilter(
                TARGET,
                "Target must be a green or white creature an opponent controls"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetPermanentAndTrackWithSourceEffect());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ReturnAllCardsExiledWithSourceEffect());
    }
}
