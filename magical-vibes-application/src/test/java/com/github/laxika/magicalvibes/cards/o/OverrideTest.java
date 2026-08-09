package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OverrideTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when the controller cannot pay for each artifact")
    void countersWhenControllerCannotPayForArtifacts() {
        harness.addToBattlefield(player2, new MyrSire());
        harness.addToBattlefield(player2, new MyrSire());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast, only 1 left to pay

        harness.setHand(player2, List.of(new Override()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can pay one for each artifact")
    void controllerCanPayForArtifacts() {
        harness.addToBattlefield(player2, new MyrSire());
        harness.addToBattlefield(player2, new MyrSire());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4); // 2 to cast, 2 to pay

        harness.setHand(player2, List.of(new Override()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
