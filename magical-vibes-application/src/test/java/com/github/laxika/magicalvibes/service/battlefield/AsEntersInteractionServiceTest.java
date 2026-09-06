package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsEntersInteractionServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private PlayerInputService playerInputService;
    @Mock private AmountEvaluationService amountEvaluationService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private PermanentCounterSupport permanentCounterSupport;
    @Mock private EtbTriggerService etbTriggerService;

    private AsEntersInteractionService service;
    private GameData gameData;
    private UUID controllerId;

    @BeforeEach
    void setUp() {
        service = new AsEntersInteractionService(gameQueryService, playerInputService,
                amountEvaluationService, predicateEvaluationService, permanentCounterSupport,
                etbTriggerService);
        controllerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Player");
        gameData.orderedPlayerIds.add(controllerId);
        gameData.playerBattlefields.put(
                controllerId, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    void ordinaryEntryContinuesToEtbTriggerCollection() {
        Card card = creature("Ordinary Creature");
        gameData.playerBattlefields.get(controllerId).add(new Permanent(card));

        service.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);

        verify(etbTriggerService).processCreatureETBEffects(
                gameData, controllerId, card, null, false, 0, 0, false,
                java.util.List.of(), java.util.List.of(), java.util.List.of());
    }

    @Test
    void parityChoiceDefersCreatureEnterTriggersAndPreservesCastContext() {
        Card card = creature("Parity Creature");
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseManaValueParityOnEnterEffect());
        Permanent entering = new Permanent(card);
        gameData.playerBattlefields.get(controllerId).add(entering);
        UUID target = UUID.randomUUID();

        service.handleCreatureEnteredBattlefield(gameData, controllerId, card, target, true,
                2, 3, true, java.util.List.of(target));

        verify(playerInputService).beginManaValueParityChoice(gameData, controllerId,
                new ChoiceContext.ManaValueParityChoice(entering.getId(), card, target, true,
                        2, 3, true, java.util.List.of(target), java.util.List.of(), java.util.List.of()));
        verifyNoInteractions(etbTriggerService);
    }

    @Test
    void tributePromptsOpponentBeforeCollectingEtbTriggers() {
        UUID opponentId = UUID.randomUUID();
        gameData.orderedPlayerIds.add(opponentId);
        Card card = creature("Tribute Creature");
        card.addEffect(EffectSlot.STATIC, new TributeEffect(2));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(card));
        when(gameQueryService.getOpponentId(gameData, controllerId)).thenReturn(opponentId);

        service.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);

        assertThat(gameData.pendingMayAbilities).hasSize(1);
        assertThat(gameData.pendingMayAbilities.getFirst().choicePlayerId()).isEqualTo(opponentId);
        verify(playerInputService).processNextMayAbility(gameData);
        verifyNoInteractions(etbTriggerService);
    }

    @Test
    void chooseAnotherCreatureStartsEntryChoiceBeforeEtbTriggers() {
        Card card = creature("Bodyguard");
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseAnotherCreatureOnEnterEffect());
        Permanent other = new Permanent(creature("Other Creature"));
        Permanent entering = new Permanent(card);
        gameData.playerBattlefields.get(controllerId).add(other);
        gameData.playerBattlefields.get(controllerId).add(entering);
        when(gameQueryService.isCreature(gameData, other)).thenReturn(true);

        service.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);

        verify(playerInputService).beginPermanentChoice(
                org.mockito.ArgumentMatchers.eq(gameData),
                org.mockito.ArgumentMatchers.eq(controllerId),
                anyList(), anyString());
        verifyNoInteractions(etbTriggerService);
    }

    @Test
    void chooseCounterTypesStartsTheRequiredNumberOfDistinctChoices() {
        Card card = creature("Grimdancer");
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCounterTypeOnEnterEffect(2,
                        CounterType.MENACE, CounterType.DEATHTOUCH, CounterType.LIFELINK));
        gameData.playerBattlefields.get(controllerId).add(new Permanent(card));

        service.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);

        var context = forClass(ChoiceContext.AsEntersCounterTypeChoice.class);
        verify(playerInputService).beginAsEntersCounterTypeChoice(eq(gameData), context.capture());
        assertThat(context.getValue().exiledCardCount()).isEqualTo(2);
        assertThat(context.getValue().counterTypes()).containsExactly(
                CounterType.MENACE, CounterType.DEATHTOUCH, CounterType.LIFELINK);
        assertThat(context.getValue().requireDifferentCounterTypes()).isTrue();
        verifyNoInteractions(etbTriggerService);
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }
}
