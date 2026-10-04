package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnarledEffigy.class, GrizzlyBears.class, LlanowarElves.class})
class GnarledEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on target creature")
    void putsMinusCounterOnTargetCreature() {
        Permanent effigy = addCreatureReady(player1, new GnarledEffigy());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(effigy);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, index, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(effigy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A -1/-1 counter reduces a 1/1 to 0 toughness and it dies")
    void killsOneToughnessCreature() {
        Permanent effigy = addCreatureReady(player1, new GnarledEffigy());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addToBattlefield(player2, new LlanowarElves());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(effigy);
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, index, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Ability fizzles if target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent effigy = addCreatureReady(player1, new GnarledEffigy());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(effigy);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, index, null, targetId);

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate immediately and target its controller's creature")
    void activatesImmediatelyOnOwnCreature() {
        Permanent effigy = harness.addToBattlefieldAndReturn(player1, new GnarledEffigy());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(effigy.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability resolves even if Gnarled Effigy leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new GnarledEffigy());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with fewer than four mana")
    void requiresFourMana() {
        Permanent effigy = harness.addToBattlefieldAndReturn(player1, new GnarledEffigy());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(effigy.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent effigy = harness.addToBattlefieldAndReturn(player1, new GnarledEffigy());
        effigy.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreature() {
        Permanent effigy = harness.addToBattlefieldAndReturn(player1, new GnarledEffigy());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledEffigy());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(effigy.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
