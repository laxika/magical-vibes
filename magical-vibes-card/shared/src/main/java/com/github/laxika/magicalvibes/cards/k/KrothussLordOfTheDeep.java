package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetMatches;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Back face of {@link RunoStromkirk}. */
public class KrothussLordOfTheDeep extends Card {

    public KrothussLordOfTheDeep() {
        PermanentPredicate anotherAttackingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        PermanentPredicate seaMonster = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.KRAKEN),
                new PermanentHasSubtypePredicate(CardSubtype.LEVIATHAN),
                new PermanentHasSubtypePredicate(CardSubtype.OCTOPUS),
                new PermanentHasSubtypePredicate(CardSubtype.SERPENT)
        ));
        CreateTokenCopyOfTargetPermanentEffect copy = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), false, false, false, true,
                false, false, null, Set.of(), false, List.of(), List.of(), false, false,
                new FixedIfTargetMatches(seaMonster, 2, 1), false, Set.of(), true);

        target(new PermanentPredicateTargetFilter(
                anotherAttackingCreature, "Target must be another attacking creature"));
        addEffect(EffectSlot.ON_ATTACK, copy);
    }
}
