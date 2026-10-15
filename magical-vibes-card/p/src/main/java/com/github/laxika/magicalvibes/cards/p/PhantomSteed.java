package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFC", collectorNumber = "18")
public class PhantomSteed extends Card {

    public PhantomSteed() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        target(new ControlledPermanentPredicateTargetFilter(
                anotherCreature, "Target must be another creature you control"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());

        CreateTokenCopyOfTargetPermanentEffect tokenCopy = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(CardSubtype.ILLUSION), Set.of(), null, null, Map.of(),
                false, false, false, true,
                false, false, null, Set.of(),
                false,
                Map.of(EffectSlot.ON_ENTER_BATTLEFIELD,
                        List.of(new SacrificeAtEndOfCombatEffect())),
                List.of(), false, false, new Fixed(1), false, Set.of(), false);
        addEffect(EffectSlot.ON_ATTACK,
                new CreateTokenCopyOfExiledCreatureWithSourceEffect(tokenCopy));
    }
}
