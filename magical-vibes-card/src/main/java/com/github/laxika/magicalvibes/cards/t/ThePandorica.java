package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseInLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPermanentWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "25")
@CardRegistration(set = "WHO", collectorNumber = "343")
public class ThePandorica extends Card {

    private static final PermanentPredicate ANOTHER_NONLAND_PERMANENT = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentIsLandPredicate()),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    public ThePandorica() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.TARGET, ANOTHER_NONLAND_PERMANENT),
                        new PhaseOutTargetPermanentWhileSourceTappedEffect(ANOTHER_NONLAND_PERMANENT)
                ),
                "{1}{W}, {T}: Untap another target nonland permanent, then it phases out. It can't phase in for as long as The Pandorica remains tapped. When The Pandorica becomes untapped or leaves the battlefield, that permanent phases in. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        ANOTHER_NONLAND_PERMANENT,
                        "Target must be another nonland permanent"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new PhaseInLinkedPermanentEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new PhaseInLinkedPermanentEffect());
    }
}
