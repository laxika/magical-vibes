package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelkieHedgeMageTest extends BaseCardTest {

    // ===== Forest gate: may gain 3 life =====

    @Test
    @DisplayName("With two Forests, ETB may gain 3 life")
    void forestGateGainsLife() {
        addLands(player1, 2, 0);
        castSelkie();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ConditionalEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Declining the Forest trigger gains no life")
    void forestGateDeclinedGainsNoLife() {
        addLands(player1, 2, 0);
        castSelkie();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With only one Forest the life-gain trigger does not fire")
    void oneForestDoesNotTrigger() {
        addLands(player1, 1, 0);
        castSelkie();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Island gate: may return target tapped creature to hand =====

    @Test
    @DisplayName("With two Islands, ETB may return a tapped creature to its owner's hand")
    void islandGateBouncesTappedCreature() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities(); // resolve creature spell -> trigger-time target prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve ConditionalEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the Island trigger leaves the tapped creature on the battlefield")
    void islandGateDeclinedLeavesCreature() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("With two Islands but no tapped creature, the bounce trigger finds no legal target")
    void islandGateNoTappedCreatureNoTrigger() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2); // left untapped
        castSelkie();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("With only one Island the bounce trigger does not fire")
    void oneIslandDoesNotTrigger() {
        addLands(player1, 0, 1);
        Permanent bears = addBears(player2);
        bears.tap();
        castSelkie();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bears.getId()));
    }

    // ===== Neither gate met =====

    @Test
    @DisplayName("With no Forests or Islands, neither ability triggers")
    void neitherGateTriggers() {
        castSelkie();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private void castSelkie() {
        harness.setHand(player1, List.of(new SelkieHedgeMage()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

    private void addLands(Player player, int forests, int islands) {
        for (int i = 0; i < forests; i++) {
            harness.addToBattlefield(player, new Forest());
        }
        for (int i = 0; i < islands; i++) {
            harness.addToBattlefield(player, new Island());
        }
    }

    private Permanent addBears(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }
}
