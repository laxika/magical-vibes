package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "4ED", collectorNumber = "361")
public class MishrasFactory extends Card {

    public MishrasFactory() {
        // {T}: Add {C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        // {1}: This land becomes a 2/2 Assembly-Worker artifact creature until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AnimatePermanentsEffect(2, 2,
                        List.of(CardSubtype.ASSEMBLY_WORKER),
                        Set.of(),
                        null,
                        Set.of(CardType.ARTIFACT))),
                "{1}: This land becomes a 2/2 Assembly-Worker artifact creature until end of turn. It's still a land."
        ));
        // {T}: Target Assembly-Worker creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{T}: Target Assembly-Worker creature gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.ASSEMBLY_WORKER)
                        )),
                        "Target must be an Assembly-Worker creature"
                )
        ));
    }
}
