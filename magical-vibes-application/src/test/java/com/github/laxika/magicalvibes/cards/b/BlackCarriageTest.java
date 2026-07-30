package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlackCarriageTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Black Carriage does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent carriage = addCarriageReady(player1);
        carriage.tap();

        advanceToNextTurn(player2);

        assertThat(carriage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherPermanentsStillUntap() {
        Permanent carriage = addCarriageReady(player1);
        carriage.tap();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player1.getId()).add(bears);

        advanceToNextTurn(player2);

        assertThat(carriage.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a creature during your upkeep untaps Black Carriage")
    void sacrificingCreatureUntapsCarriage() {
        Permanent carriage = addCarriageReady(player1);
        carriage.tap();
        // Grizzly Bears is the only other creature, so the sacrifice cost auto-picks it.
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId(player1));
        harness.passBothPriorities();

        assertThat(carriage.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the untap ability outside your upkeep")
    void cannotActivateDuringMainPhase() {
        addCarriageReady(player1);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot activate the untap ability during an opponent's upkeep")
    void cannotActivateDuringOpponentUpkeep() {
        addCarriageReady(player1);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    private UUID bearsId(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    private Permanent addCarriageReady(Player player) {
        Permanent perm = new Permanent(new BlackCarriage());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
