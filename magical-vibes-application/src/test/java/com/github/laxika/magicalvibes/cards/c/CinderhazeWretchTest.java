package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CinderhazeWretchTest extends BaseCardTest {

    // ===== Discard ability =====

    @Test
    @DisplayName("Tap ability makes the target player discard a card")
    void tapAbilityDiscards() {
        addReadyWretch(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tap ability cannot be activated during opponent's turn")
    void tapAbilityOnlyDuringYourTurn() {
        addReadyWretch(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    // ===== Untap ability =====

    @Test
    @DisplayName("Untap ability untaps the Wretch and puts a -1/-1 counter on it as a cost")
    void untapAbilityUntapsAndAddsCounter() {
        Permanent wretch = addReadyWretch(player1);
        wretch.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        // Cost is paid immediately on activation.
        assertThat(wretch.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();
        assertThat(wretch.isTapped()).isFalse();
    }

    private Permanent addReadyWretch(Player player) {
        Permanent wretch = new Permanent(new CinderhazeWretch());
        wretch.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wretch);
        return wretch;
    }
}
