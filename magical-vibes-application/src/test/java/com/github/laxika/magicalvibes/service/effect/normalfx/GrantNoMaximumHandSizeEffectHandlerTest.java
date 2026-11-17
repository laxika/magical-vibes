package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GrantNoMaximumHandSizeEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Rest-of-game duration is a permanent floating effect on the player")
    void restOfGameAddsPlayerToPermanentSet() {
        resolve(Duration.PERMANENT);

        assertThat(gd.floatingEffectsOnPlayer(player1Id, GrantNoMaximumHandSizeEffect.class))
                .extracting(FloatingContinuousEffect::duration).contains(Duration.PERMANENT);
        assertThat(gd.floatingEffectsOnPlayer(player1Id, GrantNoMaximumHandSizeEffect.class))
                .extracting(FloatingContinuousEffect::duration).doesNotContain(Duration.UNTIL_YOUR_NEXT_TURN);
    }

    @Test
    @DisplayName("Until-next-turn duration is a floating effect until the player's next turn")
    void untilNextTurnAddsPlayerToTemporarySet() {
        resolve(Duration.UNTIL_YOUR_NEXT_TURN);

        assertThat(gd.floatingEffectsOnPlayer(player1Id, GrantNoMaximumHandSizeEffect.class))
                .extracting(FloatingContinuousEffect::duration).contains(Duration.UNTIL_YOUR_NEXT_TURN);
        assertThat(gd.floatingEffectsOnPlayer(player1Id, GrantNoMaximumHandSizeEffect.class))
                .extracting(FloatingContinuousEffect::duration).doesNotContain(Duration.PERMANENT);
    }

    @Test
    @DisplayName("Logs the rest-of-game wording")
    void logsRestOfGameGrant() {
        resolve(Duration.PERMANENT);

        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no maximum hand size for the rest of the game")));
    }

    @Test
    @DisplayName("Logs the until-next-turn wording")
    void logsUntilNextTurnGrant() {
        resolve(Duration.UNTIL_YOUR_NEXT_TURN);

        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no maximum hand size until their next turn")));
    }

    private void resolve(Duration duration) {
        Card card = createCard("Spellbook");
        GrantNoMaximumHandSizeEffect effect = new GrantNoMaximumHandSizeEffect(duration);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);
    }
}
