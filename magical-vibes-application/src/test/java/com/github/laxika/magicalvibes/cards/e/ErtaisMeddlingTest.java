package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErtaisMeddlingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving exiles the target spell with X delay counters on it")
    void exilesTargetSpellWithDelayCounters() {
        GrizzlyBears bears = castBearsAndMeddle(2);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        GameData.DelayedSpellExile pending = gd.delayedSpellExiles.getFirst();
        assertThat(pending.cardId()).isEqualTo(bears.getId());
        assertThat(pending.controllerId()).isEqualTo(player1.getId());
        assertThat(pending.counters()).isEqualTo(2);
    }

    @Test
    @DisplayName("The exiled spell's controller's upkeep removes one delay counter")
    void upkeepRemovesOneDelayCounter() {
        castBearsAndMeddle(2);

        triggerUpkeep(player1);

        assertThat(gd.delayedSpellExiles).singleElement()
                .extracting(GameData.DelayedSpellExile::counters).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only the exiled spell's controller's upkeep removes a delay counter")
    void opponentUpkeepDoesNotRemoveCounter() {
        castBearsAndMeddle(2);

        triggerUpkeep(player2);

        assertThat(gd.delayedSpellExiles).singleElement()
                .extracting(GameData.DelayedSpellExile::counters).isEqualTo(2);
    }

    @Test
    @DisplayName("When the last delay counter is removed the card goes back onto the stack and resolves")
    void lastCounterPutsSpellBackOntoTheStack() {
        castBearsAndMeddle(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, delay-counter trigger goes on the stack
        harness.passBothPriorities(); // resolve the trigger — Grizzly Bears goes back onto the stack

        assertThat(gd.delayedSpellExiles).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).singleElement()
                .extracting(e -> e.getCard().getName()).isEqualTo("Grizzly Bears");

        harness.passBothPriorities(); // resolve Grizzly Bears

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Two delay counters delay the spell until the controller's second upkeep")
    void twoCountersTakeTwoUpkeeps() {
        castBearsAndMeddle(2);

        triggerUpkeep(player1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        triggerUpkeep(player1);
        harness.passBothPriorities(); // resolve Grizzly Bears

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    /**
     * player1 casts Grizzly Bears, player2 responds with Ertai's Meddling for the given X and both
     * players let the Meddling resolve.
     */
    private GrizzlyBears castBearsAndMeddle(int xValue) {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ErtaisMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, xValue);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, xValue, bears.getId());
        harness.passBothPriorities();

        return bears;
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on the stack
        harness.passBothPriorities(); // resolve the triggered ability
    }
}
