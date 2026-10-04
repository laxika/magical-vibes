package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.s.SilvercoatLion;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarruksPackleader.class, GarruksCompanion.class, RuneclawBear.class, SpinedWurm.class,
        HonorOfThePure.class, SilvercoatLion.class})
class GarruksPackleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers may-draw when another creature with power 3+ enters under controller's control")
    void triggersWhenPower3OrGreaterCreatureEnters() {
        harness.addToBattlefield(player1, new GarruksPackleader());

        // Cast Spined Wurm (5/4) â€” power 5 should trigger
        harness.castFromHand(player1, new SpinedWurm(), "{4}{G}");
        harness.passBothPriorities(); // Resolve Spined Wurm

        // MayEffect goes on stack â€” resolve it to get prompt
        harness.passBothPriorities();

        // May ability should be queued â€” accept it, inner effect resolves inline
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

        harness.castFromHand(player1, new SpinedWurm(), "{4}{G}");
        harness.passBothPriorities(); // Resolve Spined Wurm

        // MayEffect goes on stack â€” resolve it to get prompt
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when creature with power less than 3 enters")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new GarruksPackleader());

        // Cast Runeclaw Bear (2/2) â€” power 2 should NOT trigger
        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");
        harness.passBothPriorities(); // Resolve Runeclaw Bear

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when opponent's creature with power 3+ enters")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setHand(player1, List.of());

        // Opponent's Spined Wurm (5/4) enters â€” should NOT trigger Packleader
        harness.getGameData().activePlayerId = player2.getId();
        harness.castFromHand(player2, new SpinedWurm(), "{4}{G}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItself() {
        // Cast Garruk's Packleader (4/4) â€” should not trigger itself
        harness.castFromHand(player1, new GarruksPackleader(), "{4}{G}");
        harness.passBothPriorities(); // Resolve Packleader

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers for creature with exactly power 3 (Garruk's Companion)")
    void triggersForExactlyPower3() {
        harness.addToBattlefield(player1, new GarruksPackleader());

        // Cast Garruk's Companion (3/2) â€” power exactly 3 should trigger
        harness.castFromHand(player1, new GarruksCompanion(), "{G}{G}");
        harness.passBothPriorities(); // Resolve Garruk's Companion

        // MayEffect goes on stack â€” resolve it to get prompt
        harness.passBothPriorities();

        // May ability should be queued â€” accept it, inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Another Packleader entering triggers only the existing Packleader")
    void anotherPackleaderTriggersExistingPackleader() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.castFromHand(player1, new GarruksPackleader(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Power includes continuous boosts as the creature enters")
    void triggersForCreatureBoostedToThreePowerAsItEnters() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.addToBattlefield(player1, new HonorOfThePure());
        harness.castFromHand(player1, new SilvercoatLion(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }
}
