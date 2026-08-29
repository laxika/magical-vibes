package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TriggerTargetCollectorTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private TargetLegalityService targetLegalityService;

    private TriggerTargetCollector collector;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private Card sourceCard;

    @BeforeEach
    void setUp() {
        collector = new TriggerTargetCollector(gameQueryService, predicateEvaluationService, targetLegalityService);

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerBattlefields.put(player1Id, new ArrayList<>());
        gd.playerBattlefields.put(player2Id, new ArrayList<>());
        gd.status = GameStatus.RUNNING;

        sourceCard = new Card();
        sourceCard.setName("Test Source");

        // Non-strict stub: most tests don't have permanents, but the helper still calls isCreature
        // on each when canTargetPermanents is true.
        lenient().when(gameQueryService.isCreature(any(), any())).thenReturn(true);
        // The any-target narrowing evaluates the declared TargetPredicate's permanent restriction
        // instead of re-implementing it, which is a real predicate evaluation — a mocked one would
        // silently reject every candidate. Delegate that one call to a genuine evaluator over the
        // same mocked GameQueryService; TargetFilter checking stays mocked, as the tests rely on.
        PredicateEvaluationService realPredicates = new PredicateEvaluationService(gameQueryService);
        lenient().when(predicateEvaluationService.matchesPermanentPredicate(
                        any(Permanent.class), any(PermanentPredicate.class), any(FilterContext.class)))
                .thenAnswer(invocation -> realPredicates.matchesPermanentPredicate(
                        invocation.<Permanent>getArgument(0),
                        invocation.<PermanentPredicate>getArgument(1),
                        invocation.<FilterContext>getArgument(2)));
    }

    @Test
    @DisplayName("Player-only effect with no target filter yields every player")
    void playerOnlyNoFilterYieldsAllPlayers() {
        List<CardEffect> effects = List.of(new MillEffect(1, MillRecipient.TARGET_PLAYER));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, null, player1Id, sourceCard, TriggerTargetCollector.Options.DEATH);

        assertThat(result.canTargetPlayers()).isTrue();
        assertThat(result.canTargetPermanents()).isFalse();
        assertThat(result.opponentOnly()).isFalse();
        assertThat(result.validTargets()).containsExactly(player1Id, player2Id);
    }

    @Test
    @DisplayName("PlayerRelationPredicate.OPPONENT excludes the controller from valid targets")
    void opponentOnlyExcludesController() {
        TargetFilter filter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Must be an opponent");
        List<CardEffect> effects = List.of(new MillEffect(1, MillRecipient.TARGET_PLAYER));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, filter, player1Id, sourceCard, TriggerTargetCollector.Options.DEATH);

        assertThat(result.opponentOnly()).isTrue();
        assertThat(result.validTargets()).containsExactly(player2Id);
    }

    @Test
    @DisplayName("Opponent-only filter is honoured consistently for ATTACK and END_STEP options")
    void opponentOnlyHonouredForEveryOptionPreset() {
        TargetFilter filter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Must be an opponent");
        List<CardEffect> effects = List.of(new MillEffect(1, MillRecipient.TARGET_PLAYER));

        assertThat(collector.collect(gd, effects, filter, player1Id, sourceCard,
                TriggerTargetCollector.Options.ATTACK).validTargets())
                .containsExactly(player2Id);
        assertThat(collector.collect(gd, effects, filter, player1Id, sourceCard,
                TriggerTargetCollector.Options.END_STEP).validTargets())
                .containsExactly(player2Id);
    }

    @Test
    @DisplayName("ConditionalEffect delegates canTarget* to the wrapped effect in every mode")
    void conditionalEffectDelegatesTargeting() {
        // The generic ConditionalEffect delegates canTargetPlayer to its wrapped effect,
        // so target visibility no longer depends on the unwrapConditional option.
        List<CardEffect> effects = List.of(new ConditionalEffect(new DidntAttack(), new MillEffect(1, MillRecipient.TARGET_PLAYER)));

        TriggerTargetCollector.Result withUnwrap = collector.collect(
                gd, effects, null, player1Id, sourceCard, TriggerTargetCollector.Options.END_STEP);
        assertThat(withUnwrap.canTargetPlayers()).isTrue();
        assertThat(withUnwrap.validTargets()).containsExactly(player1Id, player2Id);

        TriggerTargetCollector.Result withoutUnwrap = collector.collect(
                gd, effects, null, player1Id, sourceCard, TriggerTargetCollector.Options.DEATH);
        assertThat(withoutUnwrap.canTargetPlayers()).isTrue();
        assertThat(withoutUnwrap.validTargets()).containsExactly(player1Id, player2Id);
    }

    @Test
    @DisplayName("MayEffect applies the wrapped effect's permanent target restriction")
    void mayEffectAppliesWrappedPermanentTargetRestriction() {
        Permanent ownCreature = new Permanent(new Card());
        Permanent opponentCreature = new Permanent(new Card());
        gd.playerBattlefields.get(player1Id).add(ownCreature);
        gd.playerBattlefields.get(player2Id).add(opponentCreature);

        List<CardEffect> effects = List.of(new MayEffect(
                new ExileTargetPermanentUntilSourceLeavesEffect(false,
                        new PermanentIsSpecificPermanentPredicate(opponentCreature.getId())),
                "Exile target permanent?"));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, null, player1Id, sourceCard, TriggerTargetCollector.Options.ATTACK);

        assertThat(result.validTargets()).containsExactly(opponentCreature.getId());
    }

    @Test
    @DisplayName("MayPayManaEffect applies a targeted else effect when the paid branch is empty")
    void mayPayManaEffectAppliesElseEffectTargetRestriction() {
        Permanent ownCreature = new Permanent(new Card());
        Permanent opponentCreature = new Permanent(new Card());
        gd.playerBattlefields.get(player1Id).add(ownCreature);
        gd.playerBattlefields.get(player2Id).add(opponentCreature);

        List<CardEffect> effects = List.of(new MayPayManaEffect(
                "{U}", null, "Pay {U}?",
                new DestroyTargetPermanentEffect(
                        new PermanentIsSpecificPermanentPredicate(opponentCreature.getId()))));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, null, player1Id, sourceCard, TriggerTargetCollector.Options.ATTACK);

        assertThat(result.validTargets()).containsExactly(opponentCreature.getId());
    }

    @Test
    @DisplayName("DEATH option skips non-creature permanents")
    void deathCreaturesOnly() {
        Permanent creature = new Permanent(new Card());
        Permanent noncreature = new Permanent(new Card());
        gd.playerBattlefields.get(player2Id).add(creature);
        gd.playerBattlefields.get(player2Id).add(noncreature);
        lenient().when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
        lenient().when(gameQueryService.isCreature(gd, noncreature)).thenReturn(false);

        // DealDamageToAnyTargetEffect can target players AND permanents.
        List<CardEffect> effects = List.of(new DealDamageToAnyTargetEffect(1));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, null, player1Id, sourceCard, TriggerTargetCollector.Options.DEATH);

        assertThat(result.canTargetPermanents()).isTrue();
        assertThat(result.validTargets()).contains(creature.getId());
        assertThat(result.validTargets()).doesNotContain(noncreature.getId());
    }

    @Test
    @DisplayName("DEATH option with an explicit permanent filter allows non-creature targets")
    void deathExplicitPermanentFilterAllowsNonCreatures() {
        Permanent noncreature = new Permanent(new Card());
        gd.playerBattlefields.get(player2Id).add(noncreature);
        lenient().when(gameQueryService.isCreature(gd, noncreature)).thenReturn(false);
        lenient().when(predicateEvaluationService.matchesPermanentPredicate(
                        any(Permanent.class), any(PermanentPredicate.class), any(FilterContext.class)))
                .thenReturn(true);

        // Fire Snake's "destroy target land": the filter's predicate governs, not creaturesOnly.
        TargetFilter filter = new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(), "Target can be any permanent");
        List<CardEffect> effects = List.of(new DealDamageToAnyTargetEffect(1));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, filter, player1Id, sourceCard, TriggerTargetCollector.Options.DEATH);

        assertThat(result.validTargets()).contains(noncreature.getId());
    }

    @Test
    @DisplayName("ATTACK any-target excludes lands (creature / planeswalker / player only)")
    void attackAnyTargetExcludesLands() {
        Permanent creature = new Permanent(new Card());
        Permanent land = new Permanent(new Card());
        gd.playerBattlefields.get(player2Id).add(creature);
        gd.playerBattlefields.get(player2Id).add(land);
        lenient().when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
        lenient().when(gameQueryService.isCreature(gd, land)).thenReturn(false);

        List<CardEffect> effects = List.of(new DealDamageToAnyTargetEffect(1));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, null, player1Id, sourceCard, TriggerTargetCollector.Options.ATTACK);

        assertThat(result.canTargetPlayers()).isTrue();
        assertThat(result.canTargetPermanents()).isTrue();
        assertThat(result.validTargets())
                .contains(player1Id, player2Id, creature.getId())
                .doesNotContain(land.getId());
    }

    /**
     * The narrowing comes from evaluating {@code TargetPredicates.anyTarget()} rather than a
     * hand-rolled type check, so it admits whatever CR 115.4 admits and stays layer-aware: a
     * permanent that is a planeswalker only after layer 4 is offered, and one that stopped being a
     * creature is not.
     */
    @Test
    @DisplayName("ATTACK any-target offers a layer-4 planeswalker that is not a creature")
    void attackAnyTargetOffersLayeredPlaneswalker() {
        Permanent planeswalker = new Permanent(new Card());
        gd.playerBattlefields.get(player2Id).add(planeswalker);
        lenient().when(gameQueryService.isCreature(gd, planeswalker)).thenReturn(false);
        lenient().when(gameQueryService.isPlaneswalker(gd, planeswalker)).thenReturn(true);

        List<CardEffect> effects = List.of(new DealDamageToAnyTargetEffect(1));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, null, player1Id, sourceCard, TriggerTargetCollector.Options.ATTACK);

        assertThat(result.validTargets()).contains(planeswalker.getId());
    }

    @Test
    @DisplayName("UPKEEP option honors creature-you-control filters")
    void upkeepControlledPermanentFilter() {
        Permanent ownCreature = new Permanent(new Card());
        Permanent opponentCreature = new Permanent(new Card());
        gd.playerBattlefields.get(player1Id).add(ownCreature);
        gd.playerBattlefields.get(player2Id).add(opponentCreature);
        lenient().when(predicateEvaluationService.matchesFilters(
                        eq(ownCreature), anySet(), any(FilterContext.class)))
                .thenReturn(true);
        lenient().when(predicateEvaluationService.matchesFilters(
                        eq(opponentCreature), anySet(), any(FilterContext.class)))
                .thenReturn(false);

        List<CardEffect> effects = List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        TriggerTargetCollector.Result result = collector.collect(
                gd, effects, TargetFilters.creatureYouControl(), player1Id, sourceCard,
                TriggerTargetCollector.Options.UPKEEP);

        assertThat(result.validTargets()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
    }
}
