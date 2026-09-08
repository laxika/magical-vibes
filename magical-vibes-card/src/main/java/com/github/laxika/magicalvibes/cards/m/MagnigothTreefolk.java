package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "82")
public class MagnigothTreefolk extends Card {

    public MagnigothTreefolk() {
        addDomainLandwalk(CardSubtype.PLAINS);
        addDomainLandwalk(CardSubtype.ISLAND);
        addDomainLandwalk(CardSubtype.SWAMP);
        addDomainLandwalk(CardSubtype.MOUNTAIN);
        addDomainLandwalk(CardSubtype.FOREST);
    }

    private void addDomainLandwalk(CardSubtype landType) {
        var landOfType = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentHasSubtypePredicate(landType)
        ));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(landOfType),
                new GrantEffectEffect(
                        new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(landOfType, true),
                        GrantScope.SELF
                )
        ));
    }
}
