package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElementalBondTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a creature with power 3 or greater enters under controller's control")
    void drawsWhenPower3OrGreaterCreatureEnters() {
        harness.addToBattlefield(player1, new ElementalBond());

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Hill Giant
        harness.passBothPriorities(); // Resolve the draw trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when a creature with power less than 3 enters")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new ElementalBond());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Grizzly Bears

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature with power 3 or greater enters")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new ElementalBond());
        harness.setHand(player1, List.of());

        harness.addToBattlefield(player2, new HillGiant());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
