package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelekineticSliver.class, BonesplitterSliver.class, Forest.class, AshcoatBear.class})
class TelekineticSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Telekinetic Sliver grants itself the ability to tap any permanent")
    void grantsAbilityToItself() {
        Permanent telekineticSliver = addCreatureReady(player1, new TelekineticSliver());
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(telekineticSliver.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Sliver gains the ability and can tap a land")
    void grantsAbilityToAnotherSliver() {
        addCreatureReady(player1, new TelekineticSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonesplitterSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(otherSliver.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Sliver also gains the ability")
    void grantsAbilityToOpposingSliver() {
        addCreatureReady(player1, new TelekineticSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonesplitterSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(opposingSliver.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSliver() {
        addCreatureReady(player1, new TelekineticSliver());
        addCreatureReady(player1, new AshcoatBear());
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability cannot target a player")
    void rejectsPlayerTarget() {
        addCreatureReady(player1, new TelekineticSliver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickSliverCannotPayTapCost() {
        addCreatureReady(player1, new TelekineticSliver());
        Permanent otherSliver = harness.addToBattlefieldAndReturn(player1, new BonesplitterSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(otherSliver.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void tapCostIsPaidBeforeTargetIsTapped() {
        Permanent sliver = addCreatureReady(player1, new TelekineticSliver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(sliver.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tappedSliverCannotActivateAgain() {
        Permanent sliver = addCreatureReady(player1, new TelekineticSliver());
        sliver.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetItselfEvenThoughTapCostTapsIt() {
        Permanent sliver = addCreatureReady(player1, new TelekineticSliver());

        harness.activateAbility(player1, 0, null, sliver.getId());
        harness.passBothPriorities();

        assertThat(sliver.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
