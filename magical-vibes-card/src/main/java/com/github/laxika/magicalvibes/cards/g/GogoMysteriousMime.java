package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "56")
@CardRegistration(set = "FIC", collectorNumber = "153")
public class GogoMysteriousMime extends Card {

    public GogoMysteriousMime() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        target(new ControlledPermanentPredicateTargetFilter(
                anotherCreature,
                "Target must be another creature you control"
        )).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
                                "Gogo, Mysterious Mime", Set.of()),
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF),
                        new MatchingCreaturesMustAttackThisTurnEffect(new PermanentIsSourceCardPredicate()),
                        new BoostTargetCreatureEffect(2, 0),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK)
                ),
                "Have Gogo become a copy of another creature you control?"
        ));
    }
}
