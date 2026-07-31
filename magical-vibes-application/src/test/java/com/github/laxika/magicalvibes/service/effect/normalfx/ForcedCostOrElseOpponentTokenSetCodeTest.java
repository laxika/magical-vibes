package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForcedCostOrElseOpponentTokenSetCodeTest {

    @Mock
    private DestructionSupport destructionSupport;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private LibraryExileSupport libraryExileSupport;
    @Mock
    private AmountEvaluationService amountEvaluationService;

    private ForcedCostOrElseEffectHandler handler;
    private GameData gd;
    private UUID payerId;
    private UUID opponentId;

    @BeforeEach
    void setUp() {
        payerId = UUID.randomUUID();
        opponentId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", payerId, "Payer");
        gd.orderedPlayerIds.add(payerId);
        gd.orderedPlayerIds.add(opponentId);
        gd.playerIds.add(payerId);
        gd.playerIds.add(opponentId);
        gd.playerBattlefields.put(payerId, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));
        handler = new ForcedCostOrElseEffectHandler(
                destructionSupport, gameQueryService, predicateEvaluationService,
                playerInputService, libraryExileSupport, amountEvaluationService);
    }

    @Test
    @DisplayName("Opponent token cost passes the source set code into createTokenForPlayer")
    void createOpponentTokensPassesSourceSetCode() {
        when(gameQueryService.getOpponentId(gd, payerId)).thenReturn(opponentId);
        CreateTokenEffect survivor = new CreateTokenEffect("Survivor", 1, 1, CardColor.RED,
                List.of(CardSubtype.SURVIVOR), Set.of(), Set.of());
        OpponentCreatesTokensCost cost = new OpponentCreatesTokensCost(2, survivor);

        handler.createOpponentTokens(gd, payerId, cost, "Varchild's War-Riders", "ALL");

        verify(destructionSupport, times(2))
                .createTokenForPlayer(gd, opponentId, survivor, "Varchild's War-Riders", "ALL");
    }
}
