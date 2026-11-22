package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChoosePlayerEffectHandlerTest {

    @Mock
    private SubjectResolutionService subjectResolutionService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private GameLogService gameLogService;

    private final UUID controllerId = UUID.randomUUID();
    private final UUID firstId = UUID.randomUUID();
    private final UUID secondId = UUID.randomUUID();
    private final ChoosePlayerEffect effect = ChoosePlayerEffect.opponent();
    private GameData gameData;
    private StackEntry entry;
    private ChoosePlayerEffectHandler handler;

    @BeforeEach
    void setUp() {
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Controller");
        gameData.playerIdToName.put(secondId, "Second");
        Card source = new Card();
        source.setName("Source");
        entry = new StackEntry(source, controllerId);
        handler = new ChoosePlayerEffectHandler(subjectResolutionService, playerInputService, gameLogService);
    }

    @Test
    @DisplayName("With several players the controller is asked, and only an offered player can be recorded")
    void asksAndRecordsAnOfferedPlayer() {
        when(subjectResolutionService.players(gameData, entry, effect, effect.players()))
                .thenReturn(List.of(firstId, secondId));

        handler.resolve(gameData, entry, effect);

        verify(playerInputService).beginPlayerChoice(eq(gameData), eq(controllerId), eq(List.of(firstId, secondId)),
                anyString());
        assertThat(entry.getResolutionResults().outcome(StepKind.CHOOSE).playerIds()).isEmpty();

        var context = (PermanentChoiceContext.RecordChosenPlayer) gameData.interaction.permanentChoiceContext();
        gameData.pendingEffectResolutionEntry = entry;
        assertThatThrownBy(() -> handler.completeChoice(gameData, controllerId, context))
                .isInstanceOf(IllegalStateException.class);
        handler.completeChoice(gameData, secondId, context);

        assertThat(entry.getResolutionResults().outcome(StepKind.CHOOSE).playerIds()).containsExactly(secondId);
        verify(gameLogService).append(eq(gameData), any());
    }
}
