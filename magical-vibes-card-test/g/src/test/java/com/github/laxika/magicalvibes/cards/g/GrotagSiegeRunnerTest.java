package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.OvergrownBattlement;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrotagSiegeRunner.class, OvergrownBattlement.class})
class GrotagSiegeRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself, destroys a creature with defender, and deals 2 damage to its controller")
    void destroysDefenderAndDealsDamageToController() {
        harness.addToBattlefield(player1, new GrotagSiegeRunner());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new OvergrownBattlement());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.assertInGraveyard(player1, "Grotag Siege-Runner");
        harness.assertOnBattlefield(player2, "Overgrown Battlement");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grotag Siege-Runner");
        harness.assertInGraveyard(player2, "Overgrown Battlement");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage even when the targeted defender is saved from destruction")
    void dealsDamageWhenDestructionIsPrevented() {
        harness.addToBattlefield(player1, new GrotagSiegeRunner());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new OvergrownBattlement());
        wall.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Overgrown Battlement");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target a creature without defender")
    void cannotTargetCreatureWithoutDefender() {
        harness.addToBattlefield(player1, new GrotagSiegeRunner());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrotagSiegeRunner());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with defender");
    }

    @Test
    @DisplayName("Can destroy your own defender and damages you rather than the opponent")
    void canTargetOwnDefender() {
        harness.addToBattlefield(player1, new GrotagSiegeRunner());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new OvergrownBattlement());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Overgrown Battlement");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not deal damage if the only target leaves before resolution")
    void doesNotDealDamageWhenTargetLeaves() {
        harness.addToBattlefield(player1, new GrotagSiegeRunner());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new OvergrownBattlement());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, wall.getId());
        gd.playerBattlefields.get(player2.getId()).remove(wall);
        gd.playerHands.get(player2.getId()).add(wall.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grotag Siege-Runner");
        harness.assertLife(player2, 20);
    }
}
