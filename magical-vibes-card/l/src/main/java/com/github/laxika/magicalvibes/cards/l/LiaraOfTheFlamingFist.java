package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithControlledCreatureOrGraveyardCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "73")
public class LiaraOfTheFlamingFist extends Card {

    public LiaraOfTheFlamingFist() {
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new BoostAllOwnCreaturesEffect(1, 1,
                        new PermanentSharesNameWithControlledCreatureOrGraveyardCreaturePredicate()));

        PermanentAllOfPredicate anotherNontokenCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{W}",
                List.of(new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.DOUBLE_TEAM), GrantScope.TARGET)),
                "{1}{R}{W}: Another target nontoken creature you control gains first strike and double team until end of turn."
                        + " Activate only as a sorcery and only once.",
                new ControlledPermanentPredicateTargetFilter(
                        anotherNontokenCreature,
                        "Target must be another nontoken creature you control"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withMaxActivationsPerGame(1));
    }
}
