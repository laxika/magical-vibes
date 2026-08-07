package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GrantNoMaximumHandSizeEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Rest-of-game duration adds the player to the permanent set only")
    void restOfGameAddsPlayerToPermanentSet() {
        resolve(NoMaximumHandSizeDuration.REST_OF_GAME);

        assertThat(gd.playersWithNoMaximumHandSize).contains(player1Id);
        assertThat(gd.playersWithNoMaximumHandSizeUntilNextTurn).doesNotContain(player1Id);
    }

    @Test
    @DisplayName("Until-next-turn duration adds the player to the temporary set only")
    void untilNextTurnAddsPlayerToTemporarySet() {
        resolve(NoMaximumHandSizeDuration.UNTIL_NEXT_TURN);

        assertThat(gd.playersWithNoMaximumHandSizeUntilNextTurn).contains(player1Id);
        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player1Id);
    }

    @Test
    @DisplayName("Logs the rest-of-game wording")
    void logsRestOfGameGrant() {
        resolve(NoMaximumHandSizeDuration.REST_OF_GAME);

        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no maximum hand size for the rest of the game")));
    }

    @Test
    @DisplayName("Logs the until-next-turn wording")
    void logsUntilNextTurnGrant() {
        resolve(NoMaximumHandSizeDuration.UNTIL_NEXT_TURN);

        verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("no maximum hand size until their next turn")));
    }

    private void resolve(NoMaximumHandSizeDuration duration) {
        Card card = createCard("Spellbook");
        GrantNoMaximumHandSizeEffect effect = new GrantNoMaximumHandSizeEffect(duration);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);
    }
}
