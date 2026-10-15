package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgottenCreation.class, GrizzlyBears.class, Island.class, Shock.class})
class ForgottenCreationTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep ability may discard the hand and draw the same number")
    void upkeepMayDiscardHandAndDrawThatMany() {
        harness.addToBattlefield(player1, new ForgottenCreation());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card instanceof Island);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the upkeep ability leaves the hand unchanged")
    void decliningUpkeepAbilityDoesNothing() {
        harness.addToBattlefield(player1, new ForgottenCreation());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card instanceof Shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Skulk prevents a creature with greater power from blocking")
    void skulkPreventsGreaterPowerCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new ForgottenCreation());
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setPowerModifier(2);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    @DisplayName("Accepting with an empty hand draws no cards")
    void acceptingWithEmptyHandDrawsNothing() {
        harness.addToBattlefield(player1, new ForgottenCreation());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new ForgottenCreation());
        harness.setHand(player1, List.of(new Island()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The number of cards drawn is determined when the ability resolves")
    void countsHandAtResolution() {
        harness.addToBattlefield(player1, new ForgottenCreation());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Island(), new Island()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Skulk allows a creature with equal power to block")
    void skulkAllowsEqualPowerBlocker() {
        Permanent attacker = addCreatureReady(player1, new ForgottenCreation());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ForgottenCreation());
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }
}
