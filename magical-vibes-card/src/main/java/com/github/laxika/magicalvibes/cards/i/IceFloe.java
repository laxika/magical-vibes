package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "420")
public class IceFloe extends Card {

    public IceFloe() {
        // Static: "You may choose not to untap this land during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // {T}: Tap target creature without flying that's attacking you. It doesn't untap during
        // its controller's untap step for as long as this land remains tapped.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        DoesntUntapEffect.targetWhileSourceTapped()
                ),
                "{T}: Tap target creature without flying that's attacking you. It doesn't untap during its controller's untap step for as long as Ice Floe remains tapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingSourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                        )),
                        "Target must be a creature without flying that's attacking you"
                )
        ));
    }
}
