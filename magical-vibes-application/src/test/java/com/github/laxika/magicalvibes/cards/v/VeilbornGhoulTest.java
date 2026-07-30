package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeilbornGhoulTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Veilborn Ghoul cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent ghoul = new Permanent(new VeilbornGhoul());
        ghoul.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ghoul);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Accepting the Swamp trigger returns Veilborn Ghoul from graveyard to hand")
    void swampReturnsFromGraveyardOnAccept() {
        VeilbornGhoul ghoul = new VeilbornGhoul();
        harness.setGraveyard(player1, List.of(ghoul));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Swamp()));
        harness.castCreature(player1, 0); // play the Swamp
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(ghoul.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(ghoul.getId()));
    }

    @Test
    @DisplayName("Declining the Swamp trigger keeps Veilborn Ghoul in the graveyard")
    void swampDeclineKeepsInGraveyard() {
        VeilbornGhoul ghoul = new VeilbornGhoul();
        harness.setGraveyard(player1, List.of(ghoul));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Swamp()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(ghoul.getId()));
    }

    @Test
    @DisplayName("A non-Swamp land entering does not trigger")
    void nonSwampLandDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new VeilbornGhoul()));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Island()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Swamp an opponent controls entering does not trigger")
    void opponentSwampDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new VeilbornGhoul()));
        prepareMain(player2);

        harness.setHand(player2, List.of(new Swamp()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).isEmpty();
    }
}
