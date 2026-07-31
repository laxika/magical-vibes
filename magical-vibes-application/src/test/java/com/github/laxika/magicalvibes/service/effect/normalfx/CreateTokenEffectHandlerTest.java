package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTokenEffectHandlerTest {

    @Mock
    private PermanentControlSupport permanentControlSupport;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AmountEvaluationService amountEvaluationService;

    private CreateTokenEffectHandler handler;
    private GameData gd;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", playerId, "Player1");
        gd.orderedPlayerIds.add(playerId);
        gd.playerIds.add(playerId);
        gd.playerBattlefields.put(playerId, Collections.synchronizedList(new ArrayList<>()));
        handler = new CreateTokenEffectHandler(permanentControlSupport, gameQueryService, amountEvaluationService);
    }

    @Test
    @DisplayName("Passes the source card's set code into token creation")
    void passesSourceSetCode() {
        CreateTokenEffect effect = new CreateTokenEffect("Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of());
        Card source = new Card();
        source.setName("Raise the Alarm");
        source.setSetCode("M10");
        StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, source, playerId, "Raise the Alarm",
                List.of(effect), 0);

        when(amountEvaluationService.evaluate(eq(gd), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(1);
        when(permanentControlSupport.applyCreateToken(eq(gd), eq(playerId), eq(effect), eq(1), eq("M10"), eq(1), eq(1)))
                .thenReturn(List.of());

        handler.resolve(gd, entry, effect);

        verify(permanentControlSupport).applyCreateToken(gd, playerId, effect, 1, "M10", 1, 1);
    }
}
