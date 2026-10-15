package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.f.FierceWitchstalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakhameAdversary.class, GrizzlyBears.class, Gingerbrute.class, FierceWitchstalker.class})
class OakhameAdversaryTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2} less when an opponent controls a green permanent")
    void reducedCostWhenOpponentControlsGreenPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OakhameAdversary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the reduced cost without an opponent green permanent")
    void fullCostWithoutOpponentGreenPermanent() {
        harness.setHand(player1, List.of(new OakhameAdversary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Draws a card when it deals combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        addCreatureReady(player1, new OakhameAdversary()).setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void ownGreenPermanentDoesNotReduceCost() {
        harness.addToBattlefield(player1, new OakhameAdversary());
        harness.setHand(player1, List.of(new OakhameAdversary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void colorlessOpponentPermanentDoesNotReduceCost() {
        harness.addToBattlefield(player2, new Gingerbrute());
        harness.setHand(player1, List.of(new OakhameAdversary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void canPayFullCostWithoutGreenOpponentPermanent() {
        harness.setHand(player1, List.of(new OakhameAdversary()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void multipleGreenPermanentsDoNotMultiplyReduction() {
        harness.addToBattlefield(player2, new OakhameAdversary());
        harness.addToBattlefield(player2, new OakhameAdversary());
        harness.setHand(player1, List.of(new OakhameAdversary()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionDoesNotRemoveGreenManaRequirement() {
        harness.addToBattlefield(player2, new OakhameAdversary());
        harness.setHand(player1, List.of(new OakhameAdversary()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void defendingPlayersAdversaryDrawsForItsControllerWhenItAttacks() {
        addCreatureReady(player2, new OakhameAdversary()).setAttacking(true);
        harness.setLibrary(player2, List.of(new Gingerbrute(), new OakhameAdversary()));
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void blockedCombatKillsLargerCreatureWithoutDrawing() {
        Permanent attacker = addCreatureReady(player1, new OakhameAdversary());
        attacker.setAttacking(true);
        addCreatureReady(player2, new FierceWitchstalker());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player1, "Oakhame Adversary");
        harness.assertInGraveyard(player2, "Fierce Witchstalker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore);
    }
}
