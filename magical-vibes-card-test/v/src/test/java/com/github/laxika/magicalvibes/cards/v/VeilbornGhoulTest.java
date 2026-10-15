package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeilbornGhoul.class, WalkingCorpse.class, Island.class, Swamp.class})
class VeilbornGhoulTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Veilborn Ghoul cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player2, new VeilbornGhoul());
        ghoul.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

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
        harness.playLand(player1, 0);
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
        harness.playLand(player1, 0);

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
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Swamp an opponent controls entering does not trigger")
    void opponentSwampDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new VeilbornGhoul()));
        prepareMain(player2);

        harness.setHand(player2, List.of(new Swamp()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each graveyard copy triggers independently and returns only itself")
    void multipleCopiesHaveIndependentChoices() {
        VeilbornGhoul first = new VeilbornGhoul();
        VeilbornGhoul second = new VeilbornGhoul();
        WalkingCorpse other = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(first, second, other));
        prepareMain(player1);
        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getId())
                .isIn(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(other);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(other);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Ghoul on the battlefield or in hand does not trigger when a Swamp enters")
    void doesNotTriggerOutsideGraveyard() {
        harness.addToBattlefield(player1, new VeilbornGhoul());
        harness.setHand(player1, List.of(new Swamp(), new VeilbornGhoul()));
        prepareMain(player1);

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Veilborn Ghoul");
        harness.assertOnBattlefield(player1, "Veilborn Ghoul");
    }

    @Test
    @DisplayName("A Ghoul removed from the graveyard before resolution cannot be returned")
    void exiledGhoulIsNotReturned() {
        VeilbornGhoul ghoul = new VeilbornGhoul();
        harness.setGraveyard(player1, List.of(ghoul));
        prepareMain(player1);
        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(ghoul));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ghoul);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
