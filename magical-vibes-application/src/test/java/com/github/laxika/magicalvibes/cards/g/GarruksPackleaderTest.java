package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GarruksPackleaderTest extends BaseCardTest {

    // ===== Triggers when power >= 3 creature enters =====

    @Test
    @DisplayName("Triggers may-draw when another creature with power 3+ enters under controller's control")
    void triggersWhenPower3OrGreaterCreatureEnters() {
        harness.addToBattlefield(player1, new GarruksPackleader());

        // Cast Hill Giant (3/3) — power 3 should trigger
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Hill Giant

        // MayEffect goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // May ability should be queued — accept it, inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Player1 drew a card (started with 0 cards in hand after setHand was consumed)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Player may decline to draw a card")
    void playerMayDeclineToDraw() {
        harness.addToBattlefield(player1, new GarruksPackleader());

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Hill Giant

        // MayEffect goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Does NOT trigger for power < 3 =====

    @Test
    @DisplayName("Does not trigger when creature with power less than 3 enters")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new GarruksPackleader());

        // Cast Grizzly Bears (2/2) — power 2 should NOT trigger
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Grizzly Bears

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Does not trigger for opponent's creatures =====

    @Test
    @DisplayName("Does not trigger when opponent's creature with power 3+ enters")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setHand(player1, List.of());

        // Opponent's Hill Giant (3/3) enters — should NOT trigger Packleader
        harness.addToBattlefield(player2, new HillGiant());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Does not trigger for itself entering =====

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItself() {
        // Cast Garruk's Packleader (4/4) — should not trigger itself
        harness.setHand(player1, List.of(new GarruksPackleader()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Packleader

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Triggers for exactly power 3 =====

    @Test
    @DisplayName("Triggers for creature with exactly power 3 (Garruk's Companion)")
    void triggersForExactlyPower3() {
        harness.addToBattlefield(player1, new GarruksPackleader());

        // Cast Garruk's Companion (3/2) — power exactly 3 should trigger
        harness.setHand(player1, List.of(new GarruksCompanion()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Garruk's Companion

        // MayEffect goes on stack — resolve it to get prompt
        harness.passBothPriorities();

        // May ability should be queued — accept it, inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
