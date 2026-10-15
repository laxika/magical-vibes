package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonsLawkeeper.class, RuneclawBear.class, Plains.class})
class GideonsLawkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps the target creature and taps the Lawkeeper as a cost")
    void resolvingTapsTarget() {
        Permanent lawkeeper = addCreatureReady(player1, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(lawkeeper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature its controller owns")
    void canTapOwnCreature() {
        addCreatureReady(player1, new GideonsLawkeeper());
        Permanent own = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, own.getId());
        harness.passBothPriorities();

        assertThat(own.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the ability twice in a turn because it requires tapping")
    void cannotActivateTwice() {
        addCreatureReady(player1, new GideonsLawkeeper());
        Permanent first = addCreatureReady(player2, new RuneclawBear());
        Permanent second = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the ability without mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Pays costs immediately but taps the target only on resolution")
    void costsArePaidBeforeResolution() {
        Permanent lawkeeper = addCreatureReady(player1, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(lawkeeper.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent lawkeeper = addCreatureReady(player1, new GideonsLawkeeper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lawkeeper.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target an already tapped creature")
    void canTargetTappedCreature() {
        Permanent lawkeeper = addCreatureReady(player1, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(lawkeeper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent lawkeeper = addCreatureReady(player1, new GideonsLawkeeper());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, lawkeeper.getId());
        harness.passBothPriorities();

        assertThat(lawkeeper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent lawkeeper = addCreatureReady(player1, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lawkeeper);
        gd.playerGraveyards.get(player1.getId()).add(lawkeeper.getCard());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves and returns is a new permanent")
    void doesNotTapReturnedTarget() {
        Permanent lawkeeper = addCreatureReady(player1, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());

        harness.passBothPriorities();

        assertThat(returned.isTapped()).isFalse();
        assertThat(lawkeeper.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
