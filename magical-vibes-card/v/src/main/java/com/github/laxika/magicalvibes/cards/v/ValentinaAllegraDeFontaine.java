package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "671")
public class ValentinaAllegraDeFontaine extends Card {

    public ValentinaAllegraDeFontaine() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                CardSubtype.HERO,
                GrantScope.OWN_CREATURES,
                false,
                new PermanentHasSubtypePredicate(CardSubtype.VILLAIN)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawDiscardAndConniveEffect(true)),
                "{T}: Target Hero you control connives. Activate only as a sorcery.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.HERO))),
                        "Target must be a Hero you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
