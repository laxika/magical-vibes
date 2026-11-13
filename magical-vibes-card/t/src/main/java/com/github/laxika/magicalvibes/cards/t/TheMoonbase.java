package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardsFromGraveyardToBattlefieldFaceDownAsCybermenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "591")
public class TheMoonbase extends Card {

    public TheMoonbase() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{2}",
                        List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF,
                                GrantDuration.END_OF_TURN)),
                        "{2}: This creature gains flying until end of turn. Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED),
                GrantScope.ALL_CREATURES));

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(new GraveyardCardPredicateTargetFilter(creature, GraveyardSearchScope.OPPONENT_GRAVEYARD), 0, 99)
                .addTrigger(Triggers.chaosEnsues(),
                        new ReturnTargetCreatureCardsFromGraveyardToBattlefieldFaceDownAsCybermenEffect());
    }
}
