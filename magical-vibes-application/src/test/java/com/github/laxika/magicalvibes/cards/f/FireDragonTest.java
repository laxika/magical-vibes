package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireDragon.class, GrizzlyBears.class, Island.class, Mountain.class})
class FireDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Fire Dragon enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFireDragon(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fire Dragon");
    }

    @Test
    @DisplayName("Deals damage equal to Mountains you control")
    void dealsDamageEqualToControlledMountains() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        castFireDragon(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // 2 Mountains -> 2 damage kills the 2/2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts Mountains when the ETB ability resolves")
    void countsMountainsWhenAbilityResolves() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFireDragon(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell

        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With no Mountains it deals 0 damage")
    void noMountainsDealsNoDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        castFireDragon(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts only Mountains, not other basic land types")
    void countsOnlyMountains() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFireDragon(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts only your Mountains, not opponent's")
    void countsOnlyControllerMountains() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        castFireDragon(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Controller has 0 Mountains, so 0 damage — Grizzly Bears survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target a creature you control")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 own
        castFireDragon(player1, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new FireDragon()));
        harness.addMana(player1, ManaColor.RED, 9);

        UUID targetId = harness.getPermanentId(player2, "Island");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Must target itself when it is the only creature")
    void mustTargetItselfWhenOnlyCreature() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.setHand(player1, List.of(new FireDragon()));
        harness.addMana(player1, ManaColor.RED, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell and open ETB target selection

        UUID fireDragonId = harness.getPermanentId(player1, "Fire Dragon");
        harness.handlePermanentChosen(player1, fireDragonId);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Fire Dragon");
        harness.assertInGraveyard(player1, "Fire Dragon");
    }

    // ===== Helpers =====

    private void castFireDragon(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new FireDragon()));
        harness.addMana(player1, ManaColor.RED, 9);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
