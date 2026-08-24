package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "197")
public class SarythTheVipersFang extends Card {

    public SarythTheVipersFang() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.DEATHTOUCH,
                GrantScope.OWN_CREATURES,
                new PermanentIsTappedPredicate()));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HEXPROOF,
                GrantScope.OWN_CREATURES,
                new PermanentNotPredicate(new PermanentIsTappedPredicate())));

        PermanentPredicate targetCreatureOrLandYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate()))));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, targetCreatureOrLandYouControl)),
                "{1}, {T}: Untap another target creature or land you control.",
                new PermanentPredicateTargetFilter(
                        targetCreatureOrLandYouControl,
                        "Target must be another creature or land you control")));
    }
}
