package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "PCY", collectorNumber = "75")
public class RebelInformer extends Card {

    public RebelInformer() {
        addEffect(EffectSlot.STATIC, TargetingRestrictionEffect.fromSourceColors(Set.of(CardColor.WHITE)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new PutTargetOnBottomOfLibraryEffect()),
                "{3}: Put target nontoken Rebel on the bottom of its owner's library.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.REBEL),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate())
                        )),
                        "Target must be a nontoken Rebel"
                )
        ));
    }
}
