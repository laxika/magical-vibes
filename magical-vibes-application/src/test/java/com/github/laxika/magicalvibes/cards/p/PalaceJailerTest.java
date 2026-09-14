package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PalaceJailer.class, GrizzlyBears.class})
class PalaceJailerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters, makes its controller monarch, and exiles an opposing creature")
    void entersAndExilesOpposingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castPalaceJailer(player1, bearsId);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.exileReturnOnOpponentBecomesMonarch).containsKey(player1.getId());
    }

    @Test
    @DisplayName("Returns the exiled creature when an opponent becomes monarch")
    void returnsExiledCreatureWhenOpponentBecomesMonarch() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castPalaceJailer(player1, bearsId);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID firstJailerId = harness.getPermanentId(player1, "Palace Jailer");
        harness.setHand(player2, List.of(new PalaceJailer()));
        addPalaceJailerMana(player2);
        harness.castCreature(player2, 0, firstJailerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnOpponentBecomesMonarch).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its own controller")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new PalaceJailer()));
        addPalaceJailerMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    private void castPalaceJailer(com.github.laxika.magicalvibes.model.Player player, UUID targetId) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new PalaceJailer()));
        addPalaceJailerMana(player);
        harness.castCreature(player, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addPalaceJailerMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
