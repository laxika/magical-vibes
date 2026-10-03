package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrageTyrant.class, BronzeSable.class, GrizzlyBears.class})
class BarrageTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another colorless creature and deals damage equal to its power")
    void sacrificesAnotherColorlessCreatureAndDealsPowerDamage() {
        addCreatureReady(player1, new BarrageTyrant());
        Permanent sable = addCreatureReady(player1, new BronzeSable());
        sable.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Bronze Sable");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals the damage to a target creature")
    void dealsDamageToTargetCreature() {
        addCreatureReady(player1, new BarrageTyrant());
        addCreatureReady(player1, new BronzeSable());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice the source or a colored creature")
    void requiresAnotherColorlessCreature() {
        addCreatureReady(player1, new BarrageTyrant());
        addCreatureReady(player1, new GrizzlyBears());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Test
    @DisplayName("Chosen sacrifice power is retained even when the source is sacrificed in response")
    void retainsChosenPowerAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new BarrageTyrant());
        Permanent sacrifice = addCreatureReady(player1, new BarrageTyrant());
        addCreatureReady(player1, new BarrageTyrant());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 20);
        addManaForAbility();
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.activateAbility(player1, 1, null, player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source, sacrifice);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
        harness.passBothPriorities();

        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("A tapped summoning-sick Tyrant can activate and target its controller")
    void activatesWithoutTappingOrHasteAndCanTargetController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BarrageTyrant());
        source.setSummoningSick(true);
        source.tap();
        addCreatureReady(player1, new BarrageTyrant());
        harness.setLife(player1, 20);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("An opponent's colorless creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new BarrageTyrant());
        addCreatureReady(player2, new BarrageTyrant());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Barrage Tyrant");
        harness.assertOnBattlefield(player2, "Barrage Tyrant");
    }

    @Test
    @DisplayName("The creature chosen as the target can also pay the sacrifice cost")
    void sacrificedTargetMakesAbilityFailToResolve() {
        Permanent source = addCreatureReady(player1, new BarrageTyrant());
        Permanent target = addCreatureReady(player1, new BarrageTyrant());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Barrage Tyrant");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
