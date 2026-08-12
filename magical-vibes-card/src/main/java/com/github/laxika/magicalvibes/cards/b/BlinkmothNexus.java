package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardRegistration(set = "DST", collectorNumber = "163")
public class BlinkmothNexus extends Card {

    public BlinkmothNexus() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AnimatePermanentsEffect(1, 1,
                        List.of(CardSubtype.BLINKMOTH),
                        Set.of(Keyword.FLYING),
                        null,
                        Set.of(CardType.ARTIFACT))),
                "{1}: This land becomes a 1/1 Blinkmoth artifact creature with flying until end of turn. It's still a land."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{1}, {T}: Target Blinkmoth creature gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.BLINKMOTH)
                        )),
                        "Target must be a Blinkmoth creature"
                )
        ));
    }
}
