package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EtbTriggerServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogService gameLogService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private GraveyardTargetingService graveyardTargetingService;
    @Mock private ETBTokenTargetService etbTokenTargetService;
    @Mock private AmountEvaluationService amountEvaluationService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private ConditionEvaluationService conditionEvaluationService;

    private EtbTriggerService service;
    private GameData gameData;
    private UUID controllerId;

    @BeforeEach
    void setUp() {
        service = new EtbTriggerService(gameQueryService, gameLogService, playerInputService,
                triggerCollectionService, graveyardTargetingService, etbTokenTargetService,
                new EtbEffectResolver(conditionEvaluationService), amountEvaluationService,
                predicateEvaluationService);
        controllerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Player");
        gameData.orderedPlayerIds.add(controllerId);
        gameData.playerBattlefields.put(
                controllerId, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    void targetedLandEtbQueuesTargetSelectionAtTriggerTime() {
        Card land = new Card();
        land.setName("Targeted Land");
        land.setType(CardType.LAND);
        land.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(land));

        service.processCreatureETBEffects(gameData, controllerId, land, null, false);

        assertThat(gameData.hasPendingInteraction(
                PermanentChoiceContext.ETBTokenTargetTrigger.class)).isTrue();
        assertThat(gameData.stack).isEmpty();
        verify(etbTokenTargetService).processNextETBTokenTargetTrigger(gameData);
    }

    @Test
    void reflexiveMayPayEtbQueuesWithoutChoosingTargetUntilPayment() {
        Card creature = new Card();
        creature.setName("Reflexive Creature");
        creature.setType(CardType.CREATURE);
        creature.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                MayPayManaEffect.reflexiveTarget("{2}",
                        SequenceEffect.of(new TapPermanentsEffect(TapUntapScope.TARGET),
                                new PutCounterOnTargetPermanentEffect(CounterType.STUN)),
                        "Pay {2} to tap and stun a creature?"));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(creature));

        service.processCreatureETBEffects(gameData, controllerId, creature, null, false);

        assertThat(gameData.hasPendingInteraction(
                PermanentChoiceContext.ETBTokenTargetTrigger.class)).isFalse();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getTargetId()).isNull();
    }

    @Test
    void landEntryChoiceIsPresentedBeforeTriggeredEffects() {
        Card land = new Card();
        land.setName("Color Land");
        land.setType(CardType.LAND);
        ChooseColorOnEnterEffect choice = new ChooseColorOnEnterEffect();
        land.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, choice);
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
        land.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, choice);
        Permanent permanent = new Permanent(land);
        gameData.playerBattlefields.get(controllerId).add(permanent);

        service.processLandETBEffects(gameData, controllerId, land);

        verify(playerInputService).beginBasicLandTypeChoice(
                gameData, controllerId, permanent.getId(), false, false, choice.allowedTypes());
    }
}
