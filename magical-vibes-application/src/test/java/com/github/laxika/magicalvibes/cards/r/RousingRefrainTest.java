package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RousingRefrain.class, GrizzlyBears.class})
class RousingRefrainTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana equal to the target opponent's hand size and exiles with three time counters")
    void addsManaAndIsExiledWithSuspendCounters() {
        RousingRefrain refrain = new RousingRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addRefrainMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("The generated mana survives a phase transition but expires at turn cleanup")
    void generatedManaPersistsUntilEndOfTurn() {
        RousingRefrain refrain = new RousingRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addRefrainMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void requiresAnOpponentTarget() {
        harness.setHand(player1, List.of(new RousingRefrain()));
        addRefrainMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Suspend casts it for free and exiles it again")
    void suspendCastsForFree() {
        RousingRefrain refrain = new RousingRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    private void addRefrainMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
