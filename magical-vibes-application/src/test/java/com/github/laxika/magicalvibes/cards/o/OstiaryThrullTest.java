package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OstiaryThrull.class, GruulSignet.class})
class OstiaryThrullTest extends BaseCardTest {

    @Test
    void resolvingAbilityTapsTargetCreatureAndPaysTapCost() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(thrull.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetOwnCreature() {
        addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player1, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(thrull.isTapped()).isFalse();
    }

    @Test
    void activatingAbilityConsumesWhiteMana() {
        addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(thrull.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhenSourceIsAlreadyTapped() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        thrull.tap();
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void canTargetAlreadyTappedCreature() {
        addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void fizzlesIfTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    void paysTapCostBeforeTargetIsTappedAtResolution() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(thrull.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItself() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, thrull.getId());

        assertThat(thrull.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(thrull.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent thrull = addCreatureReady(player1, new OstiaryThrull());
        Permanent target = addCreatureReady(player2, new OstiaryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(thrull);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
