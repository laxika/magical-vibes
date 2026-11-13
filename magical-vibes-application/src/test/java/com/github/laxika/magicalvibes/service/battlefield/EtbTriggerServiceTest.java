package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EtbTriggerServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogService gameLogService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private AmountEvaluationService amountEvaluationService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private ConditionEvaluationService conditionEvaluationService;
    @Mock private TargetLegalityService targetLegalityService;

    private EtbTriggerService service;
    private GameData gameData;
    private UUID controllerId;

    @BeforeEach
    void setUp() {
        service = new EtbTriggerService(gameQueryService, gameLogService, playerInputService,
                triggerCollectionService, new EtbEffectResolver(conditionEvaluationService));
        // A permanent's own enters abilities go on the stack through the stack door.
        ReflectionTestUtils.setField(service, "triggeredAbilityStackService", new TriggeredAbilityStackService(
                conditionEvaluationService, gameQueryService, new GraveyardTargetingSupport(), gameLogService,
                triggerCollectionService, amountEvaluationService, predicateEvaluationService,
                new TriggerTargetCollector(gameQueryService, predicateEvaluationService, targetLegalityService)));
        controllerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Player");
        gameData.orderedPlayerIds.add(controllerId);
        gameData.playerBattlefields.put(
                controllerId, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    void optionalEtbPreservesCastXValue() {
        Card creature = new Card();
        creature.setName("Optional draw creature");
        creature.setType(CardType.CREATURE);
        creature.addTrigger(Triggers.enters(ObjectRelation.THIS),
                new com.github.laxika.magicalvibes.model.effect.MayEffect(
                        new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(
                                new com.github.laxika.magicalvibes.model.amount.XValue()), "Draw?"));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(creature));

        service.processCreatureETBEffects(gameData, controllerId, creature, null, true,
                0, 3, false, java.util.List.of());

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(3);
    }

    @Test
    void oncePerTurnEtbSkipsAfterTheSourceHasTriggered() {
        Card creature = new Card();
        creature.setName("Once-per-turn creature");
        creature.setType(CardType.CREATURE);
        SurveilEffect surveil = new SurveilEffect(1);
        creature.addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), surveil);
        Permanent permanent = new Permanent(creature);
        gameData.playerBattlefields.get(controllerId).add(permanent);

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        assertThat(gameData.stack).singleElement()
                .extracting(entry -> entry.getEffectsToResolve().getFirst())
                .isEqualTo(surveil);
        assertThat(gameData.keyedOncePerTurnTriggersFiredThisTurn.get(permanent.getId()))
                .containsExactly(creature.getOwnEntryAbilities().getFirst().limitKey());

        gameData.stack.clear();
        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        assertThat(gameData.stack).isEmpty();
    }

    @Test
    void preservesWaterbendPaymentOnEtbTrigger() {
        Card creature = new Card();
        creature.setName("Waterbend creature");
        creature.setType(CardType.CREATURE);
        creature.addTrigger(Triggers.enters(ObjectRelation.THIS),
                new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(1));
        Permanent permanent = new Permanent(creature);
        permanent.setWaterbendCostPaid(true);
        gameData.playerBattlefields.get(controllerId).add(permanent);

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        assertThat(gameData.stack).singleElement().satisfies(entry ->
                assertThat(entry.isWaterbendCostPaid()).isTrue());
    }

    @Test
    void targetedLandEtbQueuesTargetSelectionAtTriggerTime() {
        Card land = new Card();
        land.setName("Targeted Land");
        land.setType(CardType.LAND);
        land.addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(land));

        service.processCreatureETBEffects(gameData, controllerId, land, null, false);

        assertThat(gameData.hasPendingInteraction(
                PermanentChoiceContext.AttackTriggerTarget.class)).isTrue();
        assertThat(gameData.stack).isEmpty();
        verify(triggerCollectionService).beginStackDoorChoices(gameData);
    }

    @Test
    void reflexiveMayPayEtbQueuesWithoutChoosingTargetUntilPayment() {
        Card creature = new Card();
        creature.setName("Reflexive Creature");
        creature.setType(CardType.CREATURE);
        creature.target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayPayManaEffect("{2}",
                        new QueueReflexiveAbilityEffect(SequenceEffect.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                new PutCounterOnTargetPermanentEffect(CounterType.STUN))),
                        "Pay {2} to tap and stun a creature?"));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(creature));

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        assertThat(gameData.hasPendingInteraction(
                PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getTargetId()).isNull();
    }

    @Test
    void reflexiveSacrificeEtbLeavesUpToTwoTargetGroupForReflexiveAbility() {
        Card creature = new Card();
        creature.setName("Reflexive Sacrifice Creature");
        creature.setType(CardType.CREATURE);
        creature.target(TargetFilters.creature(), 0, 2).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(
                        new SacrificePermanentThenEffect(new PermanentIsCreaturePredicate(),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                "a creature"),
                        "Sacrifice a creature?"));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(creature));

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        assertThat(gameData.hasPendingInteraction(
                PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)).isFalse();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getTargetIds()).isEmpty();
    }

    @Test
    void targetedGraveyardExileKeepsSiblingEtbEffectsOnTheSameAbility() {
        Card creature = new Card();
        creature.setName("Graveyard Target Creature");
        creature.setType(CardType.CREATURE);
        ExileGraveyardCardsEffect exile = ExileGraveyardCardsEffect.upToOneTargetFromOpponentGraveyard();
        LoseLifeEffect lifeLoss = new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT);
        // One ability of two effects (CR 603.3): two addTrigger calls would be two abilities.
        creature.addTrigger(Triggers.enters(ObjectRelation.THIS), exile, lifeLoss);
        gameData.playerBattlefields.get(controllerId).add(new Permanent(creature));

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        PermanentChoiceContext.SpellGraveyardTargetTrigger pending =
                gameData.peekPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class);
        assertThat(pending.effects()).containsExactly(exile, lifeLoss);
        assertThat(gameData.stack).isEmpty();
    }

    @Test
    void landEntryChoiceIsPresentedBeforeTriggeredEffects() {
        Card land = new Card();
        land.setName("Color Land");
        land.setType(CardType.LAND);
        ChooseColorOnEnterEffect choice = new ChooseColorOnEnterEffect();
        land.addStaticEffect(choice);
        Permanent permanent = new Permanent(land);
        gameData.playerBattlefields.get(controllerId).add(permanent);

        service.processLandETBEffects(gameData, controllerId, land);

        verify(playerInputService).beginColorChoice(
                gameData, controllerId, permanent.getId(), null, choice);
    }

    @Test
    void landEntryBasicLandTypeChoiceIsPresentedBeforeTriggeredEffects() {
        Card land = new Card();
        land.setName("Basic Type Land");
        land.setType(CardType.LAND);
        ChooseBasicLandTypeOnEnterEffect choice = new ChooseBasicLandTypeOnEnterEffect();
        land.addStaticEffect(choice);
        Permanent permanent = new Permanent(land);
        gameData.playerBattlefields.get(controllerId).add(permanent);

        service.processLandETBEffects(gameData, controllerId, land);

        verify(playerInputService).beginBasicLandTypeChoice(
                gameData, controllerId, permanent.getId(), false, false, choice.allowedTypes());
    }

    @Test
    void conditionalEntryReplacementDoesNotBecomeTriggeredAbility() {
        Card creature = new Card();
        creature.setName("Conditional Clone");
        creature.setType(CardType.CREATURE);
        creature.addStaticEffect(new ConditionalReplacementEffect(
                new Raid(), new CopyPermanentOnEnterEffect(new PermanentIsCreaturePredicate(), "creature")));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(creature));

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        assertThat(gameData.stack).isEmpty();
    }

    @Test
    void triggeredModalEvaluatesAdditionalModesConditionAtEntry() {
        Card creature = new Card();
        creature.setName("Delirium Modal Creature");
        creature.setType(CardType.CREATURE);
        ChooseOneEffect modal = ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw one", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Draw another", new DrawCardEffect(1))),
                new Delirium());
        creature.addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseOneAtTriggerTimeEffect(modal));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(creature));
        when(conditionEvaluationService.isMet(any(), any(), any())).thenReturn(true);

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        PermanentChoiceContext.TriggeredModalTrigger pending =
                gameData.peekPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class);
        assertThat(pending.effect().choicesMax()).isEqualTo(2);
        assertThat(pending.modeCount()).isNull();
    }

    @Test
    void triggeredModalWhoseAdditionalModesConditionIsFalseCarriesOneModeOnTheChoice() {
        Card creature = new Card();
        creature.setName("Delirium Modal Creature");
        creature.setType(CardType.CREATURE);
        ChooseOneEffect modal = ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw one", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Draw another", new DrawCardEffect(1))),
                new Delirium());
        creature.addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseOneAtTriggerTimeEffect(modal));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(creature));
        when(conditionEvaluationService.isMet(any(), any(), any())).thenReturn(false);

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        PermanentChoiceContext.TriggeredModalTrigger pending =
                gameData.peekPendingInteraction(PermanentChoiceContext.TriggeredModalTrigger.class);
        assertThat(pending.effect()).isEqualTo(modal);
        assertThat(pending.modeCount())
                .isEqualTo(new PermanentChoiceContext.TriggeredModalTrigger.ModeCount(1, 1));
    }
}
