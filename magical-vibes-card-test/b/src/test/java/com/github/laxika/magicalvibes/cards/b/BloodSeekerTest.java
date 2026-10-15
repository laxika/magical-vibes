package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.j.JadeMage;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodSeeker.class, RuneclawBear.class, JadeMage.class, LeylineOfSanctity.class})
class BloodSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent loses 1 life when accepting may after opponent's creature enters")
    void opponentLosesLifeWhenAccepting() {
        harness.addToBattlefield(player1, new BloodSeeker());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new RuneclawBear(), "{1}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("No life loss when declining the may")
    void noLifeLossWhenDeclining() {
        harness.addToBattlefield(player1, new BloodSeeker());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new RuneclawBear(), "{1}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for the controller's own creature entering")
    void doesNotTriggerForOwnCreature() {
        harness.addToBattlefield(player1, new BloodSeeker());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Triggers when an opponent's creature enters without being cast")
    void triggersForCreatureEnteringWithoutBeingCast() {
        harness.addToBattlefield(player1, new BloodSeeker());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new RuneclawBear());

        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Blood Seeker offers a separate choice for the same entering creature")
    void multipleSeekersHaveIndependentChoices() {
        harness.addToBattlefield(player1, new BloodSeeker());
        harness.addToBattlefield(player1, new BloodSeeker());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new RuneclawBear());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature token triggers Blood Seeker")
    void triggersForOpponentCreatureToken() {
        harness.addToBattlefield(player1, new BloodSeeker());
        harness.addToBattlefield(player2, new JadeMage());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Player hexproof does not prevent the non-targeting life loss")
    void opponentHexproofDoesNotPreventLifeLoss() {
        harness.addToBattlefield(player1, new BloodSeeker());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new RuneclawBear());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }
}
