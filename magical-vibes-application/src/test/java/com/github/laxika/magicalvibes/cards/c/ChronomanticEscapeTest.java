package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChronomanticEscape.class, GrizzlyBears.class})
class ChronomanticEscapeTest extends BaseCardTest {

    @Test
    @DisplayName("A resolved cast is exiled with three suspend time counters")
    void castExilesWithSuspendCounters() {
        ChronomanticEscape escape = new ChronomanticEscape();
        harness.setHand(player1, List.of(escape));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(escape);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(escape.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("The attack restriction lasts through the opponent's turn and expires on the controller's next turn")
    void restrictionExpiresOnControllerNextTurn() {
        ChronomanticEscape escape = new ChronomanticEscape();
        harness.setHand(player1, List.of(escape));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        gd.expireEndOfTurnFloatingEffects();
        addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A suspended escape resolves for free and starts a new suspend countdown")
    void suspendRecastsForFree() {
        ChronomanticEscape escape = new ChronomanticEscape();
        harness.setHand(player1, List.of(escape));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.exiledCardTimeCounters).containsEntry(escape.getId(), 3);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(escape.getId());
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(escape.getId(), player1.getId(), 3));
    }
}
