package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureExactStatsConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

/**
 * Unit tests for the enter-the-battlefield trigger orchestration ({@link TriggerCollectionService})
 * driving the {@link EnterTriggerCollectorService} handlers through a real
 * {@link TriggerCollectorRegistry} (mirrors {@code MiscTriggerCollectorServiceTest}'s setup).
 */
@ExtendWith(MockitoExtension.class)
class EnterTriggerCollectorServiceTest {

    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TriggeredAbilityQueueService triggeredAbilityQueueService;
    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private GameLogService gameLogService;
    @Mock private ETBTokenTargetService etbTokenTargetService;

    private TriggerCollectionService service;
    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(new EnterTriggerCollectorService(gameLogService,
                new AmountEvaluationService(predicateEvaluationService, gameQueryService), gameQueryService,
                predicateEvaluationService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService)), registry);

        service = new TriggerCollectionService(registry, gameOutcomeService, playerInputService,
                triggeredAbilityQueueService, gameQueryService, predicateEvaluationService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService),
                gameLogService, etbTokenTargetService,
                new GrantedTriggeredAbilitySupport(gameQueryService));

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
    }

    private void addAllyCreatureTrigger(EffectSlot slot, com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
        Card source = new Card();
        source.setName("Source");
        source.addEffect(slot, effect);
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));
    }

    private static Card enteringCreature(int power, int toughness) {
        Card entering = new Card();
        entering.setName("Entering Creature");
        entering.setType(CardType.CREATURE);
        entering.setPower(power);
        entering.setToughness(toughness);
        return entering;
    }

    @Test
    @DisplayName("Life-gain once-per-turn triggers fire only for the first life-gain event")
    void lifeGainOncePerTurnTriggerFiresOnlyOnce() {
        CardEffect inner = new GainLifeEffect(1);
        OncePerTurnTriggerEffect effect = new OncePerTurnTriggerEffect(inner);
        addAllyCreatureTrigger(EffectSlot.ON_CONTROLLER_GAINS_LIFE, effect);

        AtomicInteger dispatchCount = new AtomicInteger();
        registry.register(EffectSlot.ON_CONTROLLER_GAINS_LIFE, CardEffect.class,
                (match, dispatchedEffect, context) -> {
                    dispatchCount.incrementAndGet();
                    return true;
                });

        service.checkLifeGainTriggers(gd, player1Id, 3);
        service.checkLifeGainTriggers(gd, player1Id, 3);

        assertThat(dispatchCount).hasValue(1);
        assertThat(gd.oncePerTurnTriggersFiredThisTurn)
                .containsExactly(gd.playerBattlefields.get(player1Id).getFirst().getId());
    }

    @Test
    @DisplayName("Ally-creature scan skips non-creature (null toughness) entrants")
    void allyCreatureSkipsNonCreature() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));

        Card land = new Card();
        land.setName("Forest");
        land.setType(CardType.LAND);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, land, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature stat conditional gates below threshold")
    void allyCreatureConditionalGatesBelowThreshold() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));

        Card entering = enteringCreature(2, 2);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature stat conditional queues the wrapped effect when met")
    void allyCreatureConditionalQueuesWhenMet() {
        Card source = new Card();
        source.setName("Garruk's Packleader");
        source.addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));
        Card entering = enteringCreature(4, 4);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(source);
    }

    @Test
    @DisplayName("Ally-creature scan does not trigger for the entering permanent itself")
    void allyCreatureDoesNotTriggerSelf() {
        Card entering = enteringCreature(4, 4);
        entering.addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-creature source-counter conditional gates at trigger time")
    void allyCreatureSourceCounterConditionalGatesAtTriggerTime() {
        Card source = new Card();
        source.setName("Counter Source");
        source.addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE),
                        new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)));
        Permanent sourcePermanent = new Permanent(source);
        gd.playerBattlefields.get(player1Id).add(sourcePermanent);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2), 0);
        assertThat(gd.stack).isEmpty();

        sourcePermanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        service.checkAllyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2), 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Naban doubling duplicates each ally-creature trigger produced during the scan")
    void allyCreatureNabanDoubling() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(3, new GainLifeEffect(1)));
        Card entering = enteringCreature(4, 4);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 1);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Ally-creature may-transform-entering bakes the entering permanent (Vildin-Pack Alpha)")
    void allyCreatureMayTransformEntering() {
        var predicate = new CardSubtypePredicate(CardSubtype.WEREWOLF);
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(predicate, new TransformEnteringCreatureEffect()));

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        when(predicateEvaluationService.matchesCardPredicate(eq(entering), eq(predicate), eq(null), any(), any()))
                .thenReturn(true);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        MayEffect may = (MayEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
        assertThat(may.wrapped()).isInstanceOf(TransformTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Ally-creature power gate queues a may-put-counters on the entering creature (Mighty Emergence)")
    void allyCreaturePutCountersOnEntering() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(5, new PutCountersOnEnteringCreatureEffect(2)));

        Card entering = enteringCreature(8, 8);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        MayEffect may = (MayEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
        assertThat(may.wrapped()).isInstanceOf(PutCounterOnTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Ally-creature exact-stats gate bakes a mandatory put-counters on a 1/1 entrant (Sigil Captain)")
    void allyCreatureMandatoryPutCountersOnEntering1_1() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureExactStatsConditionalEffect(1, 1,
                        new PutCountersOnEnteringCreatureEffect(2, false)));

        Card entering = enteringCreature(1, 1);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isInstanceOf(PutCounterOnTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Ally-creature boost bakes a mandatory boost + haste grant on the entrant (Ogre Battledriver)")
    void allyCreatureBoostEntering() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new BoostEnteringCreatureEffect(2, 0, java.util.Set.of(Keyword.HASTE)));

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        service.checkAllyCreatureEntersTriggers(gd, player1Id, entering, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getEffectsToResolve())
                .hasSize(2)
                .hasAtLeastOneElementOfType(BoostTargetCreatureEffect.class)
                .hasAtLeastOneElementOfType(GrantKeywordEffect.class);
    }

    @Test
    @DisplayName("Ally-creature exact-stats gate skips an entrant that is not 1/1")
    void allyCreatureExactStatsGatesNon1_1() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureExactStatsConditionalEffect(1, 1,
                        new PutCountersOnEnteringCreatureEffect(2, false)));

        service.checkAllyCreatureEntersTriggers(gd, player1Id, enteringCreature(1, 2), 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ally-nontoken-creature copy trigger bakes the entering permanent as target (Necroduality)")
    void allyNontokenCreatureCreateTokenCopyBakesEntering() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new CreateTokenCopyOfTargetPermanentEffect());

        Card entering = enteringCreature(2, 2);
        Permanent enteringPermanent = new Permanent(entering);
        gd.playerBattlefields.get(player1Id).add(enteringPermanent);

        service.checkAllyNontokenCreatureEntersTriggers(gd, player1Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enteringPermanent.getId());
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isInstanceOf(CreateTokenCopyOfTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Ally-nontoken-creature copy trigger skips non-matching subtype")
    void allyNontokenCreatureCreateTokenCopySkipsNonMatchingSubtype() {
        var predicate = new CardSubtypePredicate(CardSubtype.ZOMBIE);
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(predicate, new CreateTokenCopyOfTargetPermanentEffect()));

        Card entering = enteringCreature(2, 2);
        gd.playerBattlefields.get(player1Id).add(new Permanent(entering));

        when(predicateEvaluationService.matchesCardPredicate(eq(entering), eq(predicate), eq(null), any(), any()))
                .thenReturn(false);

        service.checkAllyNontokenCreatureEntersTriggers(gd, player1Id, entering);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Any-creature scan queues a non-targeting trigger (Midnight Guard's untap)")
    void anyCreatureQueuesNonTargeting() {
        addAllyCreatureTrigger(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new UntapPermanentsEffect(TapUntapScope.SELF));

        service.checkAnyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(UntapPermanentsEffect.class);
    }

    @Test
    @DisplayName("Any-creature scan queues a generic targeting trigger for target selection")
    void anyCreatureQueuesTargeting() {
        addAllyCreatureTrigger(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new DestroyTargetPermanentEffect());

        service.checkAnyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)).isTrue();
    }

    @Test
    @DisplayName("Any-creature Soul Warden gain-life queues with no player target")
    void anyCreatureGainLife() {
        addAllyCreatureTrigger(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        service.checkAnyCreatureEntersTriggers(gd, player1Id, enteringCreature(2, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    @Test
    @DisplayName("Ally-artifact scan queues a target choice for an optional targeted effect")
    void allyArtifactMayTargetQueuesChoice() {
        addAllyCreatureTrigger(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET), "Tap target permanent?"));

        Card enteringArtifact = new Card();
        enteringArtifact.setName("Artifact");
        enteringArtifact.setType(CardType.ARTIFACT);
        service.checkAllyArtifactEntersTriggers(gd, player1Id, enteringArtifact);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.EntersTriggerTarget.class)).isTrue();
    }

    @Test
    @DisplayName("Any-permanent conditional can match an artifact entering under an opponent's control")
    void anyPermanentConditionalCanMatchAnyController() {
        UUID player2Id = UUID.randomUUID();
        gd.orderedPlayerIds.add(player2Id);
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));

        Card source = new Card();
        source.setName("Arcbound Crusher");
        source.addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentTruePredicate(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        true));
        gd.playerBattlefields.get(player1Id).add(new Permanent(source));

        Card entering = new Card();
        entering.setName("Opponent Artifact");
        entering.setType(CardType.ARTIFACT);
        gd.playerBattlefields.get(player2Id).add(new Permanent(entering));

        when(predicateEvaluationService.matchesPermanentPredicate(
                any(Permanent.class), any(PermanentPredicate.class), any(FilterContext.class))).thenReturn(true);

        service.checkAnyPermanentEntersTriggers(gd, player2Id, entering);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst())
                .isInstanceOf(PutCountersOnSelfEffect.class);
    }
}
