package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForEachDestroyedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentNameEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "118")
public class TheCurseOfFenric extends Card {

    private static final PermanentPredicate NONTOKEN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    private static final PermanentPredicate MUTANT_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.MUTANT)));
    private static final PermanentPredicate FENRIC_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNamedPredicate("Fenric")));

    public TheCurseOfFenric() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DestroyEachTargetPermanentEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new CreateTokenForEachDestroyedPermanentControllerEffect(
                        new CreateTokenEffect("Mutant", 3, 3, CardColor.GREEN,
                                List.of(CardSubtype.MUTANT),
                                Set.of(com.github.laxika.magicalvibes.model.Keyword.DEATHTOUCH), Set.of())));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I,
                List.of(new SagaChapterTargetGroup(TargetFilters.creature(), 0, 99)));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new SetTargetPermanentNameEffect("Fenric"));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new SetCardTypesEffect(Set.of(CardType.CREATURE), GrantScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new GrantSubtypeEffect(CardSubtype.HORROR, GrantScope.TARGET, true));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new SetTargetPermanentSupertypeEffect(CardSupertype.LEGENDARY, true));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.PERMANENT));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new SetBasePowerToughnessEffect(6, 6, GrantScope.TARGET, EffectDuration.PERMANENT));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II,
                List.of(new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        NONTOKEN_CREATURE, "Target must be a nontoken creature"), 1, 1)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new FightTargetsEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        MUTANT_CREATURE, "Target must be a Mutant creature"), 1, 1),
                new SagaChapterTargetGroup(new PermanentPredicateTargetFilter(
                        FENRIC_CREATURE, "Target must be a creature named Fenric"), 1, 1)));
    }
}
