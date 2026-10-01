package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KrosanCloudscraper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostShip.class, KrosanCloudscraper.class})
class GhostShipTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {U}{U}{U} grants a regeneration shield")
    void payBlueGrantsRegenerationShield() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ship.getRegenerationShield()).isEqualTo(1);
        assertThat(ship.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough blue mana")
    void cannotActivateWithoutEnoughBlueMana() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(ship.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Cannot pay Ghost Ship's blue activation with colorless mana")
    void cannotPayBlueActivationWithColorlessMana() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(ship.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shield saves Ghost Ship from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        ship.setRegenerationShield(1);
        addCreatureReady(player2, new KrosanCloudscraper());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Ghost Ship");
        assertThat(ship.isTapped()).isTrue();
        assertThat(ship.isBlocking()).isFalse();
        assertThat(ship.getMarkedDamage()).isZero();
        assertThat(ship.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ghost Ship dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        addCreatureReady(player1, new GhostShip());
        addCreatureReady(player2, new KrosanCloudscraper());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Ghost Ship");
        harness.assertInGraveyard(player1, "Ghost Ship");
    }
}
