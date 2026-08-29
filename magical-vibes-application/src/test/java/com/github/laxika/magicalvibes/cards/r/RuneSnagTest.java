package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuneSnagTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay the base cost")
    void countersWhenControllerCannotPayBaseCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can pay the base cost")
    void controllerCanPayBaseCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    @DisplayName("Adds two to the cost for each Rune Snag in all graveyards")
    void costScalesWithRuneSnagsInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new RuneSnag()));
        harness.setGraveyard(player2, List.of(new RuneSnag()));

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    @DisplayName("Counters the spell when its controller declines to pay")
    void countersWhenControllerDeclinesToPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new RuneSnag()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
