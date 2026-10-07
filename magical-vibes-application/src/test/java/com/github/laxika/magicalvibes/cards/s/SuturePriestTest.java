package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuturePriest.class})
class SuturePriestTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when accepting may after another ally creature enters")
    void gainsLifeWhenAllyCreatureEntersAccept() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new SuturePriest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → Suture Priest triggers, MayEffect on stack
        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // May ability prompt for Suture Priest's controller
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("No life gain when declining may after ally creature enters")
    void noLifeGainWhenAllyCreatureEntersDecline() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new SuturePriest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when Suture Priest itself enters the battlefield")
    void doesNotTriggerForSelfEntering() {
        harness.setHand(player1, List.of(new SuturePriest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell — Suture Priest enters
        harness.passBothPriorities();

        // No may prompt — "another creature" excludes itself
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent loses 1 life when accepting may after opponent's creature enters")
    void opponentLosesLifeWhenOpponentCreatureEntersAccept() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SuturePriest()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);

        // Resolve creature spell → Suture Priest triggers for opponent creature, MayEffect on stack
        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // May ability prompt for Suture Priest's controller (player1)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("No life loss when declining may after opponent's creature enters")
    void noLifeLossWhenOpponentCreatureEntersDecline() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SuturePriest()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent creature trigger does not fire for controller's own creature")
    void opponentTriggerDoesNotFireForOwnCreature() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new SuturePriest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → only ally trigger, no opponent trigger
        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Only the ally may ability should trigger (gain life), not the opponent one (lose life)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // Player 1 gained 1 life from ally trigger
        harness.assertLife(player1, 21);
        // Player 2 life unchanged — opponent trigger did not fire
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life-loss trigger resolves after its source leaves the battlefield")
    void opponentTriggerResolvesAfterPriestLeaves() {
        var priest = harness.addToBattlefieldAndReturn(player1, new SuturePriest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new SuturePriest());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(priest);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Priest triggers independently for another creature entering")
    void multiplePriestsTriggerIndependently() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new SuturePriest());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player1, 21);
        assertThat(gd.stack).isEmpty();
    }
}
