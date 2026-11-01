package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentFoughtThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "91")
@CardRegistration(set = "NCC", collectorNumber = "99")
public class BoxingRing extends Card {

    public BoxingRing() {
        PermanentPredicate fightTarget = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentManaValueEqualsTriggeringPermanentPredicate()
        ));

        target(new PermanentPredicateTargetFilter(fightTarget,
                        "Target must be a creature an opponent controls with the same mana value"), 0, 1)
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                        new EnteringCreatureFightsTargetCreatureEffect(fightTarget));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{T}: Create a Treasure token. Activate only if you control a creature that fought this turn."
        ).withRequiredControlledPermanents(
                new PermanentFoughtThisTurnPredicate(), 1, "a creature that fought this turn"));
    }
}
