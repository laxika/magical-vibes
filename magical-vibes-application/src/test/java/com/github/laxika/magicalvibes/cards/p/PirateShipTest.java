package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.Jinx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PirateShip.class, Island.class, GrizzlyBears.class})
class PirateShipTest extends BaseCardTest {

    // ===== State-triggered self-sacrifice =====

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenControllingNoIslands() {
        harness.setHand(player1, List.of(new PirateShip()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve state trigger → sacrificed
        harness.assertNotOnBattlefield(player1, "Pirate Ship");
        harness.assertInGraveyard(player1, "Pirate Ship");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWhileControllingIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new PirateShip()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Pirate Ship");
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        addReadyPirateShip(player1);
        harness.addToBattlefield(player2, new Island());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderControlsNoIsland() {
        addReadyPirateShip(player1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== {T}: deals 1 damage to any target =====

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyPirateShip(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 creature survives")
    void deals1DamageDoesNotKill2Toughness() {
        addReadyPirateShip(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @CardUsed(Jinx.class)
    @DisplayName("Sacrifices when its Island becomes another basic land type")
    void sacrificesWhenIslandBecomesAnotherBasicLandType() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        addCreatureReady(player1, new PirateShip());
        harness.setHand(player1, List.of(new Jinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, island.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FOREST");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Pirate Ship");
        harness.assertInGraveyard(player1, "Pirate Ship");
    }

    private void addReadyPirateShip(Player player) {
        addCreatureReady(player, new PirateShip());
        harness.addToBattlefield(player, new Island()); // keep Pirate Ship from being sacrificed
    }
}
