package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTokenForTargetPlayerEffectHandlerTest {

    @Mock
    private PermanentControlSupport permanentControlSupport;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AmountEvaluationService amountEvaluationService;
    @Mock
    private TriggerCollectionService triggerCollectionService;

    private CreateTokenForTargetPlayerEffectHandler handler;
    private GameData gameData;
    private UUID controllerId;
    private UUID targetPlayerId;

    @BeforeEach
    void setUp() {
        controllerId = UUID.randomUUID();
        targetPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Player1");
        gameData.orderedPlayerIds.add(controllerId);
        gameData.orderedPlayerIds.add(targetPlayerId);
        gameData.playerIds.add(controllerId);
        gameData.playerIds.add(targetPlayerId);
        gameData.playerBattlefields.put(controllerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerBattlefields.put(targetPlayerId, Collections.synchronizedList(new ArrayList<>()));
        handler = new CreateTokenForTargetPlayerEffectHandler(
                permanentControlSupport, gameQueryService, amountEvaluationService, triggerCollectionService);
    }

    @Test
    void checksInvestigateTriggersForTargetPlayerCreatingClue() {
        CreateTokenForTargetPlayerEffect effect = new CreateTokenForTargetPlayerEffect(
                com.github.laxika.magicalvibes.model.effect.CreateTokenEffect.ofClueToken(1));
        Card source = new Card();
        source.setName("Panther Pounce");
        source.setSetCode("MSH");
        StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, source, controllerId,
                "Panther Pounce", List.of(effect), targetPlayerId, (UUID) null);

        when(amountEvaluationService.evaluate(eq(gameData), eq(effect.tokenEffect().amount()), any())).thenReturn(1);
        when(amountEvaluationService.evaluate(eq(gameData), eq(effect.tokenEffect().power()), any())).thenReturn(0);
        when(permanentControlSupport.applyCreateToken(eq(gameData), eq(targetPlayerId),
                eq(effect.tokenEffect()), eq(1), eq("MSH"), eq(0), eq(0))).thenReturn(List.of());

        handler.resolve(gameData, entry, effect);

        verify(triggerCollectionService).checkInvestigateTriggers(gameData, targetPlayerId);
    }
}
