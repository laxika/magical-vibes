package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "187")
public class FloralSpuzzem extends Card {

    public FloralSpuzzem() {
        // Whenever this creature attacks and isn't blocked, you may destroy target artifact
        // defending player controls. If you do, this creature assigns no combat damage this turn.
        // The destruction is a cost paid as the ability resolves and "if you do" checks the payment (CR 118.12):
        // a regenerated artifact was paid for, an indestructible one can't be destroyed (CR 702.12b), so it wasn't.
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(
                        SequenceEffect.of(
                                new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentControlledByDefendingPlayerPredicate()))),
                                new ConditionalEffect(
                                        new TargetPermanentMatches(new PermanentNotPredicate(
                                                new PermanentHasKeywordPredicate(Keyword.INDESTRUCTIBLE))),
                                        new AssignNoCombatDamageEffect())),
                        "You may destroy target artifact defending player controls. If you do, this creature assigns no combat damage this turn."));
    }
}
