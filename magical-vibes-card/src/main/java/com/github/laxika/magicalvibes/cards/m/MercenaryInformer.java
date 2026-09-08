package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "15")
public class MercenaryInformer extends Card {

    public MercenaryInformer() {
        addEffect(EffectSlot.STATIC, TargetingRestrictionEffect.fromSourceColors(Set.of(CardColor.BLACK)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new PutTargetOnBottomOfLibraryEffect()),
                "{2}{W}: Put target nontoken Mercenary on the bottom of its owner's library.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.MERCENARY),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate())
                        )),
                        "Target must be a nontoken Mercenary"
                )
        ));
    }
}
