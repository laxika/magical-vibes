package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

/**
 * Unit tests for the "whenever [an object] enters" collector bodies the trigger pipeline hands its matches to
 * ({@link TriggerCollectionService#triggerZoneChange}), driving the {@link EnterTriggerCollectorService} handlers
 * through a real {@link TriggerCollectorRegistry} (mirrors {@code MiscTriggerCollectorServiceTest}'s setup). The tests
 * do the pipeline's matching themselves ({@code trigger}), and call the remaining choke-point collectors (evolve,
 * watchers, boons, emblems) where the old collector did both.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnterTriggerCollectorServiceTest {

    private static final GameQueryService.StaticBonus EMPTY_BONUS = new GameQueryService.StaticBonus(
            0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(),
            Set.of(), false, false, false, false, Set.of(), false, 0, 0, false, false);

    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TriggeredAbilityQueueService triggeredAbilityQueueService;
    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private TargetLegalityService targetLegalityService;
    @Mock private ValidTargetService validTargetService;
    @Mock private GameLogService gameLogService;
    @Mock private ETBTokenTargetService etbTokenTargetService;

    private TriggerCollectionService service;
    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;


    @BeforeEach
    void setUp() {
        lenient().when(gameQueryService.computeStaticBonus(any(GameData.class), any(Permanent.class), isNull()))
                .thenReturn(EMPTY_BONUS);
        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(new EnterTriggerCollectorService(gameLogService, gameQueryService),
                registry);

        service = new TriggerCollectionService(registry, gameOutcomeService, playerInputService,
                triggeredAbilityQueueService, gameQueryService, predicateEvaluationService,
                targetLegalityService,
                validTargetService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService),
                new AmountEvaluationService(predicateEvaluationService, gameQueryService),
                gameLogService, etbTokenTargetService,
                new GrantedTriggeredAbilitySupport(gameQueryService),
                new GraveyardTargetingSupport());

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        lenient().when(gameQueryService.isCreature(eq(gd), any(Permanent.class)))
                .thenAnswer(invocation -> ((Permanent) invocation.getArgument(1)).getCard().hasType(CardType.CREATURE));
        lenient().when(gameQueryService.getEffectivePower(eq(gd), any(Permanent.class)))
                .thenAnswer(invocation -> ((Permanent) invocation.getArgument(1)).getCard().getPower());
        lenient().when(gameQueryService.getEffectiveToughness(eq(gd), any(Permanent.class)))
                .thenAnswer(invocation -> ((Permanent) invocation.getArgument(1)).getCard().getToughness());
        lenient().when(gameQueryService.findPermanentById(eq(gd), any(UUID.class))).thenAnswer(invocation -> {
            Permanent[] found = new Permanent[1];
            gd.forEachPermanent((ownerId, permanent) -> {
                if (permanent.getId().equals(invocation.getArgument(1))) {
                    found[0] = permanent;
                }
            });
            return found[0];
        });
    }

    /** Gives {@code card} a triggered ability of {@code effect} with the first trigger condition {@code watch} accepts. */
    private static void addWatched(Card card, ZoneChangeWatch watch, CardEffect effect) {
        ZoneChangeWatch.Key key = watch.keys().getFirst();
        card.addTrigger(new TriggerCondition(key.trigger(), AbilityZones.of(key.zone())),
                TriggerOptions.DEFAULT.withMultiplicity(key.multiplicity()), effect);
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
    @DisplayName("An emblem's enters ability goes on the stack as written with the entering permanent as \"it\"")
    void emblemEntersAbilityGoesOnTheStackWithTheEnteringPermanent() {
        MayEffect draw = new MayEffect(new DrawCardEffect(1), "Draw a card?");
        Card source = emblemSource(new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                draw));
        Card landCard = new Card();
        landCard.setName("Land");
        landCard.setType(CardType.LAND);
        Permanent land = new Permanent(landCard);
        gd.playerBattlefields.get(player1Id).add(land);
        when(predicateEvaluationService.matchesPermanentPredicate(eq(land), eq(Triggers.LAND),
                any(FilterContext.class))).thenReturn(true);

        service.checkEmblemEntersTriggers(gd, player1Id, landCard);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isSameAs(source);
            assertThat(entry.getDescription()).isEqualTo("Planeswalker's emblem");
            assertThat(entry.getEffectsToResolve()).containsExactly(draw);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(land.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player1Id);
        });
    }

    @Test
    @DisplayName("An emblem's \"you control\" enters ability ignores a permanent an opponent controls, and its filter")
    void emblemEntersAbilityIgnoresAnOpponentsPermanentAndANonMatchingOne() {
        emblemSource(new TriggeredAbility(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new DrawCardEffect(1)));
        UUID opponentId = UUID.randomUUID();
        gd.orderedPlayerIds.add(opponentId);
        Card opponentLandCard = new Card();
        opponentLandCard.setName("Opponent's land");
        opponentLandCard.setType(CardType.LAND);
        Permanent opponentLand = new Permanent(opponentLandCard);
        gd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>(List.of(opponentLand))));
        Card creatureCard = enteringCreature(2, 2);
        gd.playerBattlefields.get(player1Id).add(new Permanent(creatureCard));
        when(predicateEvaluationService.matchesPermanentPredicate(eq(opponentLand), eq(Triggers.LAND),
                any(FilterContext.class))).thenReturn(true);

        service.checkEmblemEntersTriggers(gd, opponentId, opponentLandCard);
        service.checkEmblemEntersTriggers(gd, player1Id, creatureCard);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)).isFalse();
    }

    @Test
    @DisplayName("A targeted emblem enters ability queues its target choice carrying the entering permanent")
    void targetedEmblemEntersAbilityQueuesItsTargetChoice() {
        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(3);
        Card source = emblemSource(new TriggeredAbility(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL),
                damage));
        Card artifactCard = new Card();
        artifactCard.setName("Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifact = new Permanent(artifactCard);
        gd.playerBattlefields.get(player1Id).add(artifact);
        when(predicateEvaluationService.matchesPermanentPredicate(eq(artifact), eq(Triggers.ARTIFACT),
                any(FilterContext.class))).thenReturn(true);

        service.checkEmblemEntersTriggers(gd, player1Id, artifactCard);

        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.SpellTargetTriggerAnyTarget choice =
                gd.peekPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
        assertThat(choice.sourceCard()).isSameAs(source);
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.effects()).containsExactly(damage);
        assertThat(choice.triggeringPermanentId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("An emblem's \"put into your graveyard from the battlefield\" ability records the card and its "
            + "last-known permanent, and ignores a permanent an opponent owns")
    void emblemPutIntoYourGraveyardFromBattlefieldAbilityRecordsTheCard() {
        RegisterDelayedReturnDyingCreatureUnderControlEffect returnIt =
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true);
        Card source = emblemSource(new TriggeredAbility(
                Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN), returnIt));
        Card artifactCard = new Card();
        artifactCard.setName("Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifact = new Permanent(artifactCard);
        when(predicateEvaluationService.matchesPermanentPredicate(eq(artifact), eq(Triggers.ARTIFACT), any()))
                .thenReturn(true);
        UUID opponentId = UUID.randomUUID();

        service.checkEmblemPutIntoGraveyardFromBattlefieldTriggers(gd, opponentId, opponentId, artifactCard, artifact);
        assertThat(gd.stack).isEmpty();

        service.checkEmblemPutIntoGraveyardFromBattlefieldTriggers(gd, player1Id, player1Id, artifactCard, artifact);

        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isSameAs(source);
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getEffectsToResolve()).containsExactly(returnIt);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringCardId()).isEqualTo(artifactCard.getId());
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(artifact.getId());
        });
    }

    @Test
    @DisplayName("An emblem's targeted \"whenever a creature dies\" ability queues its target choice with its own "
            + "target filter, and the death reader skips a \"put into your graveyard\" ability")
    void targetedEmblemCreatureDeathAbilityQueuesItsTargetChoice() {
        GivePoisonCountersEffect poison = new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER);
        TargetFilter opponent = TargetFilters.permanentAnOpponentControls();
        Card source = emblemSource(new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), poison)
                .withTargetFilter(opponent));
        gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L, List.of(new TriggeredAbility(
                Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN),
                new DrawCardEffect(1)))));
        Card creatureCard = enteringCreature(2, 2);
        Permanent creature = new Permanent(creatureCard);
        when(predicateEvaluationService.matchesPermanentPredicate(eq(creature), eq(Triggers.CREATURE),
                any(FilterContext.class))).thenReturn(true);

        service.checkCreatureDeathRegisteredTriggers(gd, player1Id, creature, 2, 2);

        assertThat(gd.stack).isEmpty();
        PermanentChoiceContext.DeathTriggerTarget choice =
                gd.peekPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class);
        assertThat(choice.dyingCard()).isSameAs(source);
        assertThat(choice.controllerId()).isEqualTo(player1Id);
        assertThat(choice.effects()).containsExactly(poison);
        assertThat(choice.targetFilter()).isSameAs(opponent);
        assertThat(choice.triggeringPermanent().getId()).isEqualTo(creature.getId());
    }

    /** A planeswalker card whose emblem, controlled by player 1, has {@code ability}. */
    private Card emblemSource(TriggeredAbility ability) {
        Card source = new Card();
        source.setName("Planeswalker");
        gd.emblems.add(new Emblem(player1Id, List.of(), source, List.of(), 0L, List.of(ability)));
        return source;
    }
}
