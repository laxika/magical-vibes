package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({PirateShip.class, Island.class, GrizzlyBears.class, StoneRain.class, Forest.class,
        PhantasmalTerrain.class})
class PirateShipTest extends BaseCardTest {
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
    @DisplayName("Sacrifices when only the opponent controls an Island")
    void sacrificesWhenOnlyOpponentControlsIsland() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new PirateShip()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
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

    @Test
    @DisplayName("Sacrifices when its last Island leaves the battlefield")
    void sacrificesWhenLastIslandLeavesBattlefield() {
        addReadyPirateShip(player1);
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID islandId = harness.getPermanentId(player1, "Island");
        harness.castAndResolveSorcery(player1, 0, islandId);

        harness.assertOnBattlefield(player1, "Pirate Ship");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Pirate Ship");
        harness.assertInGraveyard(player1, "Pirate Ship");
    }

    @Test
    @DisplayName("A land changed into an Island satisfies the sacrifice condition")
    void transformedLandCountsAsIsland() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        harness.setHand(player1, List.of(new PirateShip()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Pirate Ship");
    }
    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island()); // keep Pirate Ship from being sacrificed
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new PirateShip());

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderControlsNoIsland() {
        harness.addToBattlefield(player1, new Island()); // keep Pirate Ship from being sacrificed

        addCreatureReady(player1, new PirateShip());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
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
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Regaining an Island does not stop an already-triggered sacrifice")
    void sacrificesEvenIfIslandIsRegainedBeforeResolution() {
        harness.setHand(player1, List.of(new PirateShip()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pirate Ship");
        harness.assertInGraveyard(player1, "Pirate Ship");
    }

    @Test
    @DisplayName("Sacrifices when the last Island becomes a Forest")
    void sacrificesWhenLastIslandLosesIslandType() {
        addReadyPirateShip(player1);
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, harness.getPermanentId(player1, "Island"));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FOREST");

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Pirate Ship");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pirate Ship");
        harness.assertInGraveyard(player1, "Pirate Ship");
    }

    @Test
    @DisplayName("Destroying one of two Islands does not cause a sacrifice")
    void survivesWhileAnotherIslandRemains() {
        addReadyPirateShip(player1);
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Island"));

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Pirate Ship");
    }

    @Test
    @DisplayName("Tap ability cannot be activated with summoning sickness")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new PirateShip());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the damage ability taps the ship and prevents another activation")
    void tapCostPreventsSecondActivation() {
        addReadyPirateShip(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(findPermanent(player1, "Pirate Ship").isTapped()).isTrue();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target its controller with the damage ability")
    void canDamageItsController() {
        addReadyPirateShip(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Can attack when a defending Forest has become an Island")
    void canAttackWhenDefendingLandGainsIslandType() {
        addReadyPirateShip(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cannot target an ordinary land with the damage ability")
    void cannotTargetOrdinaryLand() {
        addReadyPirateShip(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Island")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself and marks one damage")
    void canDamageItself() {
        addReadyPirateShip(player1);
        UUID shipId = harness.getPermanentId(player1, "Pirate Ship");

        harness.activateAbility(player1, 0, null, shipId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pirate Ship").getMarkedDamage()).isEqualTo(1);
    }

    private void addReadyPirateShip(Player player) {
        // Pirate Ship added first so it sits at battlefield index 0 for activateAbility.
        addCreatureReady(player, new PirateShip());
        harness.addToBattlefield(player, new Island()); // keep Pirate Ship from being sacrificed
    }
}
