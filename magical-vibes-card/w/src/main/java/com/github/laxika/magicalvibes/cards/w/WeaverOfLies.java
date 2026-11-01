package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceDownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasMorphAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "57")
public class WeaverOfLies extends Card {

    public WeaverOfLies() {
        addMorph("{4}{U}");

        PermanentAllOfPredicate otherMorphCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasMorphAbilityPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        target(new PermanentPredicateTargetFilter(otherMorphCreature,
                "Target must be another creature with a morph ability"), 0, 99)
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                        new TurnTargetCreatureFaceDownEffect(new PermanentHasMorphAbilityPredicate()));
    }
}
