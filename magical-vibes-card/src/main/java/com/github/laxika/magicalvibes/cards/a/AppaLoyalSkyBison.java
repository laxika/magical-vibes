package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "9")
public class AppaLoyalSkyBison extends Card {

    public AppaLoyalSkyBison() {
        PermanentPredicate anotherNonlandYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        ChooseOneEffect modes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control gains flying until end of turn",
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption(
                        "Airbend another target nonland permanent you control",
                        new AirbendTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                anotherNonlandYouControl,
                                "Target must be another nonland permanent you control"))
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, modes);
        addEffect(EffectSlot.ON_ATTACK, modes);
    }
}
