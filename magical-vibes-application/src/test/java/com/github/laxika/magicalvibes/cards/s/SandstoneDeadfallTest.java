package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandstoneDeadfall.class, Mountain.class, DwarvenGrunt.class})
class SandstoneDeadfallTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices two lands and itself to destroy an attacking creature")
    void sacrificesTwoLandsAndItselfToDestroyAttacker() {
        Permanent deadfall = addCreatureReady(player1, new SandstoneDeadfall());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sandstone Deadfall");
        harness.assertInGraveyard(player1, "Sandstone Deadfall");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Mountain"))
                .hasSize(2);
        harness.assertNotOnBattlefield(player2, "Dwarven Grunt");
        harness.assertInGraveyard(player2, "Dwarven Grunt");
        assertThat(deadfall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not destroy a creature that stops attacking before resolution")
    void fizzlesIfTargetStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new SandstoneDeadfall());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dwarven Grunt");
        harness.assertNotInGraveyard(player2, "Dwarven Grunt");
        harness.assertNotOnBattlefield(player1, "Sandstone Deadfall");
        harness.assertInGraveyard(player1, "Sandstone Deadfall");
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        Permanent deadfall = addCreatureReady(player1, new SandstoneDeadfall());
        deadfall.tap();
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertOnBattlefield(player1, "Sandstone Deadfall");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new SandstoneDeadfall());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent creature = addCreatureReady(player2, new DwarvenGrunt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without two lands to sacrifice")
    void cannotActivateWithoutTwoLands() {
        addCreatureReady(player1, new SandstoneDeadfall());
        harness.addToBattlefield(player1, new Mountain());
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays all costs before resolution and can sacrifice tapped lands")
    void paysCostsImmediatelyWithTappedLands() {
        harness.addToBattlefield(player1, new SandstoneDeadfall());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        firstLand.tap();
        secondLand.tap();
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());

        harness.assertInGraveyard(player1, "Sandstone Deadfall");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Mountain"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Dwarven Grunt");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Can destroy an attacking creature controlled by its controller")
    void canDestroyOwnAttacker() {
        harness.addToBattlefield(player1, new SandstoneDeadfall());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        Permanent attacker = addAttacker(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dwarven Grunt");
        harness.assertInGraveyard(player1, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Opponent's lands cannot pay the sacrifice cost")
    void cannotUseOpponentsLandsToPayCost() {
        harness.addToBattlefield(player1, new SandstoneDeadfall());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sandstone Deadfall");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(findPermanents(player2, "Mountain")).hasSize(2);
        harness.assertOnBattlefield(player2, "Dwarven Grunt");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new DwarvenGrunt());
        attacker.setAttacking(true);
        return attacker;
    }
}
