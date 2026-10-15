package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRebirth;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Oculus.class, GrizzlyBears.class, WrathOfGod.class, PhyrexianRebirth.class})
class OculusTest extends BaseCardTest {

    @Test
    @DisplayName("Oculus dies blocking a bigger creature, accept may ability, draws a card")
    void diesInCombatAsBlockerAcceptDraw() {
        Permanent oculusPerm = addCreatureReady(player1, new Oculus());
        oculusPerm.setBlocking(true);
        oculusPerm.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Oculus (1/1) should be dead after blocking a 2/2
        harness.assertNotOnBattlefield(player1, "Oculus");
        harness.assertInGraveyard(player1, "Oculus");

        // Resolve the MayEffect from the stack
        harness.passBothPriorities();

        // Player1 should be prompted for the may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Oculus dies blocking a bigger creature, decline may ability, no card drawn")
    void diesInCombatAsBlockerDeclineDraw() {
        Permanent oculusPerm = addCreatureReady(player1, new Oculus());
        oculusPerm.setBlocking(true);
        oculusPerm.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player1, "Oculus");

        // Resolve the MayEffect from the stack
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Oculus dies from Wrath of God, accept may ability, draws a card")
    void diesFromWrathOfGodAcceptDraw() {
        harness.addToBattlefield(player1, new Oculus());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0, null);

        GameData gd = harness.getGameData();

        harness.assertNotOnBattlefield(player1, "Oculus");
        harness.assertInGraveyard(player1, "Oculus");

        // Resolve the MayEffect from the stack
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Hand should be empty (Wrath went to graveyard) + 1 drawn card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Oculus dies from Wrath of God, decline may ability, no card drawn")
    void diesFromWrathOfGodDeclineDraw() {
        harness.addToBattlefield(player1, new Oculus());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0, null);

        GameData gd = harness.getGameData();

        // Resolve the MayEffect from the stack
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn (hand size = before - 1 for casting Wrath)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1);
    }

    @Test
    @DisplayName("Simultaneously dying Oculuses let each controller choose their own draw")
    void simultaneousDeathsGiveEachControllerAnIndependentChoice() {
        harness.addToBattlefield(player1, new Oculus());
        harness.addToBattlefield(player2, new Oculus());
        Oculus firstDraw = new Oculus();
        Oculus secondDraw = new Oculus();
        harness.setLibrary(player1, List.of(firstDraw));
        harness.setLibrary(player2, List.of(secondDraw));
        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0, null);

        harness.assertInGraveyard(player1, "Oculus");
        harness.assertInGraveyard(player2, "Oculus");
        harness.passBothPriorities();
        var firstChooser = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId();
        assertThat(firstChooser).isIn(player1.getId(), player2.getId());
        harness.handleMayAbilityChosen(firstChooser.equals(player1.getId()) ? player1 : player2, true);

        harness.passBothPriorities();
        var secondChooser = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId();
        assertThat(secondChooser).isEqualTo(firstChooser.equals(player1.getId()) ? player2.getId() : player1.getId());
        harness.handleMayAbilityChosen(secondChooser.equals(player1.getId()) ? player1 : player2, false);

        assertThat(gd.playerHands.get(firstChooser)).containsExactly(firstChooser.equals(player1.getId()) ? firstDraw : secondDraw);
        assertThat(gd.playerHands.get(secondChooser)).isEmpty();
        assertThat(gd.playerDecks.get(secondChooser)).containsExactly(secondChooser.equals(player1.getId()) ? firstDraw : secondDraw);
        assertThat(gd.stack).isEmpty();
    }
}
