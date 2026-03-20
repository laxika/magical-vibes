package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AetherAdept;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirefistAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Firefist Adept enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFirefistAdept(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Firefist Adept");
    }

    @Test
    @DisplayName("Deals damage equal to Wizards controlled (counts itself)")
    void dealsDamageCountingItself() {
        // Firefist Adept is itself a Wizard, so with no other Wizards it deals 1 damage
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        castFirefistAdept(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // 1 damage to 2/2 — Grizzly Bears survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals more damage with additional Wizards")
    void dealsMoreDamageWithMultipleWizards() {
        // Firefist Adept + AetherAdept = 2 Wizards, deals 2 damage to 2/2 — lethal
        harness.addToBattlefield(player1, new AetherAdept());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        castFirefistAdept(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts only your Wizards, not opponent's")
    void countsOnlyControllerWizards() {
        // Opponent has a Wizard, but it shouldn't count for our damage
        harness.addToBattlefield(player2, new AetherAdept()); // opponent's Wizard
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 target
        castFirefistAdept(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Only 1 Wizard controlled (Firefist Adept itself), so 1 damage to 2/2 — survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new FirefistAdept()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Firefist Adept enters the battlefield after resolution")
    void firefistAdeptEntersBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFirefistAdept(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Firefist Adept");
    }

    // ===== Helpers =====

    private void castFirefistAdept(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new FirefistAdept()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
