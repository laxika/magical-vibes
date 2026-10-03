package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.c.ChandraPyromaster;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrageOfExpendables.class, GrizzlyBears.class, FugitiveWizard.class, ChandraPyromaster.class})
class BarrageOfExpendablesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 1 damage to a player")
    void dealsOneDamageToPlayer() {
        harness.addToBattlefield(player1, new BarrageOfExpendables());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 1 damage to a creature, killing a 1-toughness one")
    void dealsOneDamageToCreature() {
        harness.addToBattlefield(player1, new BarrageOfExpendables());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());
        var wizard = harness.getPermanentId(player2, "Fugitive Wizard");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, wizard);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cannot activate without the {R}")
    void requiresRedMana() {
        harness.addToBattlefield(player1, new BarrageOfExpendables());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new BarrageOfExpendables());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays the sacrifice before resolution, even with a tapped creature")
    void sacrificeIsPaidBeforeDamage() {
        harness.addToBattlefield(player1, new BarrageOfExpendables());
        var bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("May target the creature sacrificed to pay the cost, but then does not resolve")
    void sacrificedTargetIsIllegalAtResolution() {
        harness.addToBattlefield(player1, new BarrageOfExpendables());
        var bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Barrage of Expendables");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void opponentsCreatureCannotPayCost() {
        harness.addToBattlefield(player1, new BarrageOfExpendables());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Any target includes planeswalkers")
    void damagesPlaneswalker() {
        harness.addToBattlefield(player1, new BarrageOfExpendables());
        harness.addToBattlefield(player1, new GrizzlyBears());
        var chandra = harness.addToBattlefieldAndReturn(player2, new ChandraPyromaster());
        chandra.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Chandra, Pyromaster");
    }
}
