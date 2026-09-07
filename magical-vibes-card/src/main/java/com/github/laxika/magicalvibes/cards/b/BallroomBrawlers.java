package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "3")
public class BallroomBrawlers extends Card {

    public BallroomBrawlers() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        TargetFilter targetFilter = new ControlledPermanentPredicateTargetFilter(
                anotherCreature, "Target must be another creature you control");

        target(targetFilter, 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new ChooseOneForTargetPermanentEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "First strike", grantToBoth(Keyword.FIRST_STRIKE), targetFilter),
                        new ChooseOneEffect.ChooseOneOption(
                                "Lifelink", grantToBoth(Keyword.LIFELINK), targetFilter)
                )));
    }

    private static List<CardEffect> grantToBoth(Keyword keyword) {
        return List.of(
                new GrantKeywordEffect(keyword, GrantScope.SELF),
                new GrantKeywordEffect(keyword, GrantScope.TARGET)
        );
    }
}
