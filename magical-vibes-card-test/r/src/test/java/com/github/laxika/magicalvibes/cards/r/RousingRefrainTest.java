package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SuppressionField;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RousingRefrain.class, SuppressionField.class})
class RousingRefrainTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana equal to the target opponent's hand size and exiles with three time counters")
    void addsManaAndIsExiledWithSuspendCounters() {
        RousingRefrain refrain = new RousingRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.setHand(player2, List.of(new RousingRefrain(), new RousingRefrain(), new RousingRefrain()));
        addRefrainMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

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
        harness.setHand(player2, List.of(new RousingRefrain(), new RousingRefrain()));
        addRefrainMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
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
        harness.setHand(player2, List.of(new RousingRefrain(), new RousingRefrain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    @Test
    void emptyOpponentHandStillExilesWithThreeCounters() {
        RousingRefrain refrain = new RousingRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.setHand(player2, List.of());
        addRefrainMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    @Test
    void countsOpponentHandAtResolution() {
        harness.setHand(player1, List.of(new RousingRefrain(), new RousingRefrain()));
        harness.setHand(player2, List.of(new RousingRefrain(), new RousingRefrain(), new RousingRefrain()));
        addRefrainMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new RousingRefrain()));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void decliningRecurringSuspendCastLeavesCardExiledWithoutCounters() {
        RousingRefrain refrain = new RousingRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.setHand(player2, List.of());
        addRefrainMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.suspendedSpellExiles).isEmpty();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(refrain.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({RousingRefrain.class, SuppressionField.class})
    void suppressionFieldDoesNotIncreaseSuspendCost() {
        RousingRefrain refrain = new RousingRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.addToBattlefield(player2, new SuppressionField());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.exiledCardTimeCounters).containsEntry(refrain.getId(), 3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addRefrainMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
