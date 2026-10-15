package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "119")
@CardRegistration(set = "PIP", collectorNumber = "429")
@CardRegistration(set = "PIP", collectorNumber = "647")
@CardRegistration(set = "PIP", collectorNumber = "957")
public class ShaunFatherOfSynths extends Card {

    public ShaunFatherOfSynths() {
        PermanentAllOfPredicate anotherAttackingLegendaryCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        CreateTokenCopyOfTargetPermanentEffect synthCopy = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.SYNTH), Set.of(CardType.ARTIFACT), null, null, Map.of(),
                false, false, false, true, false, false, null, Set.of(),
                false, Map.of(), List.of(), false, true, new Fixed(1), false, Set.of(), false
        );

        target(new ControlledPermanentPredicateTargetFilter(
                anotherAttackingLegendaryCreature,
                "Target must be another attacking legendary creature you control"
        )).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new MayEffect(synthCopy, "Create a tapped and attacking Synth token copy?"));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.SYNTH),
                        new PermanentControlledBySourceControllerPredicate()
                ))));
    }
}
