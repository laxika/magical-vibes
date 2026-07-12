package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpellSyphonTest extends BaseCardTest {

    /** Player2 casts Spell Syphon on player1's Grizzly Bears; returns the targeted card id. */
    private java.util.UUID castSyphonOnBears(GrizzlyBears bears) {
        harness.setHand(player1, List.of(bears));
        harness.setHand(player2, List.of(new SpellSyphon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        return bears.getId();
    }

    @Test
    @DisplayName("Counters the spell when its controller can't pay the scaled cost")
    void countersWhenControllerCannotPay() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new AirElemental()); // 2 blue permanents -> pay {2}

        GrizzlyBears bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast, only 1 left over

        castSyphonOnBears(bears);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cost equals the number of blue permanents you control (two -> {2})")
    void costScalesWithTwoBluePermanents() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addToBattlefield(player2, new AirElemental()); // 2 blue permanents -> pay {2}

        GrizzlyBears bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.GREEN, 4); // 2 to cast, 2 left over

        castSyphonOnBears(bears);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true); // pay {2}
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);

        harness.passBothPriorities(); // resolve the (uncountered) Grizzly Bears
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    @DisplayName("Cost equals the number of blue permanents you control (one -> {1})")
    void costScalesWithOneBluePermanent() {
        harness.addToBattlefield(player2, new FugitiveWizard()); // 1 blue permanent -> pay {1}

        GrizzlyBears bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast, 1 left over

        castSyphonOnBears(bears);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true); // pay {1}
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);

        harness.passBothPriorities();
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    @DisplayName("With no blue permanents the cost is {0} and the spell can't be countered")
    void zeroBluePermanentsPaysNothing() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.GREEN, 2); // exactly enough to cast, no mana left

        castSyphonOnBears(bears);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // pay {0}
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotNull();
    }
}
