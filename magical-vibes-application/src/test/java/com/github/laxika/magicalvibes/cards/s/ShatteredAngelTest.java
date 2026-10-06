package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShatteredAngel.class, Forest.class, Mountain.class})
class ShatteredAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when accepting may after opponent plays a land")
    void gainsLifeWhenOpponentPlaysLandAccept() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // Trigger on stack
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // Resolve MayEffect → prompts player

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // Resolve GainLifeEffect

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("No life gain when declining may after opponent plays a land")
    void noLifeGainWhenOpponentPlaysLandDecline() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when controller plays a land")
    void doesNotTriggerForControllerLands() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        // No trigger — only cares about opponents
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Two Shattered Angels each trigger separately when opponent plays a land")
    void twoAngelsEachTrigger() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        // Two triggers (one per Shattered Angel)
        assertThat(gd.stack).hasSize(2);

        // Resolve first MayEffect
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Triggers each time opponent plays a land on separate turns")
    void triggersOnEachLand() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // First land
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);

        // Reset for new turn
        gd.landsPlayedThisTurn.put(player2.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Second land
        harness.setHand(player2, List.of(new Mountain()));
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Triggers when an opponent's land enters without being played")
    void triggersForLandEnteringWithoutLandPlay() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An already-triggered ability still gains life after the Angel leaves")
    void triggerResolvesAfterAngelLeaves() {
        var angel = harness.addToBattlefieldAndReturn(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(angel);
        gd.playerGraveyards.get(player1.getId()).add(angel.getCard());

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Shattered Angel");
    }

    @Test
    @DisplayName("An opponent's nonland creature does not trigger the Angel")
    void doesNotTriggerForNonlandEntering() {
        harness.addToBattlefield(player1, new ShatteredAngel());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player2, new ShatteredAngel());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }
}
