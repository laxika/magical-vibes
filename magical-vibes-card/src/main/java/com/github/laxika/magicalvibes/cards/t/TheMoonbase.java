package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "591")
public class TheMoonbase extends Card {

    public TheMoonbase() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{2}",
                        List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF,
                                GrantDuration.END_OF_TURN)),
                        "{2}: This creature gains flying until end of turn. Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED),
                GrantScope.ALL_CREATURES));

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE);
        target(new GraveyardCardPredicateTargetFilter(creature, GraveyardSearchScope.OPPONENT_GRAVEYARD), 0, 99)
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new ReturnTargetCreatureCardsFromGraveyardToBattlefieldFaceDownAsCybermenEffect());
    }
}
