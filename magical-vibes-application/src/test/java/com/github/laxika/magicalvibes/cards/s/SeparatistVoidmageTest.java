package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeparatistVoidmageTest extends BaseCardTest {

    private void castVoidmage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeparatistVoidmage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("Accepting the may bounces the targeted opponent creature to its owner's hand")
    void acceptingBouncesOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Separatist Voidmage");
    }

    @Test
    @DisplayName("Declining the may leaves the creature on the battlefield")
    void decliningLeavesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Separatist Voidmage");
    }

    @Test
    @DisplayName("Can bounce a creature you control")
    void canBounceOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can bounce itself when it is the only creature")
    void canBounceItself() {
        castVoidmage();
        harness.passBothPriorities();
        UUID voidmageId = harness.getPermanentId(player1, "Separatist Voidmage");
        harness.handlePermanentChosen(player1, voidmageId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Separatist Voidmage");
        harness.assertInHand(player1, "Separatist Voidmage");
    }

    @Test
    @DisplayName("Non-creature permanents are not legal targets")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GildedLotus());
        UUID lotusId = harness.getPermanentId(player2, "Gilded Lotus");
        castVoidmage();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, lotusId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Gilded Lotus");
    }

    @Test
    @DisplayName("The target prompt appears because the Voidmage itself is always a legal target")
    void promptAppearsWithNoOtherCreatures() {
        castVoidmage();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }
}
