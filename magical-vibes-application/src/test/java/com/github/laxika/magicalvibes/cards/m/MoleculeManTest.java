package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.Zephyrim;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoleculeMan.class, GrizzlyBears.class, Forest.class, Zephyrim.class})
class MoleculeManTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a drawn nonland card for miracle {0}")
    void castsDrawnNonlandCardForZeroMana() {
        harness.addToBattlefield(player1, new MoleculeMan());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        drawAndProcessMiracleReveal();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.manaCost()).isEqualTo("{0}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not grant miracle to a land card")
    void doesNotGrantMiracleToLand() {
        harness.addToBattlefield(player1, new MoleculeMan());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        drawCard();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("A land as the first draw prevents miracle on the second draw")
    void nonlandAfterFirstDrawLandDoesNotOfferMiracle() {
        harness.addToBattlefield(player1, new MoleculeMan());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), bears));

        drawCard();
        drawCard();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Does not grant miracle to an opponent's drawn card")
    void opponentDoesNotReceiveMiracle() {
        harness.addToBattlefield(player1, new MoleculeMan());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Declining to reveal leaves the card in hand")
    void canDeclineReveal() {
        harness.addToBattlefield(player1, new MoleculeMan());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        drawAndProcessMiracleReveal();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining to cast leaves the revealed card in hand")
    void canDeclineMiracleCast() {
        harness.addToBattlefield(player1, new MoleculeMan());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        drawAndProcessMiracleReveal();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted miracle still works after Molecule Man leaves the battlefield")
    void miracleTriggerKeepsGrantedCostAfterSourceLeaves() {
        harness.addToBattlefield(player1, new MoleculeMan());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        drawAndProcessMiracleReveal();
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A card with printed miracle can also use the granted zero-cost miracle")
    void printedMiracleDoesNotPreventGrantedZeroCost() {
        harness.addToBattlefield(player1, new MoleculeMan());
        harness.setLibrary(player1, List.of(new Zephyrim()));

        drawAndProcessMiracleReveal();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        for (int choices = 0; choices < 4
                && gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice choice;
                choices++) {
            harness.handleMayAbilityChosen(player1, "{0}".equals(choice.manaCost()));
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Zephyrim");
        harness.assertNotInHand(player1, "Zephyrim");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void drawAndProcessMiracleReveal() {
        drawCard();
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
