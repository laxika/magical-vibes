package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArborElf.class, Forest.class, Island.class})
class ArborElfTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped Forest and taps Arbor Elf as the cost")
    void untapsTappedForest() {
        Permanent elf = addCreatureReady(player1, new ArborElf());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(elf.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap a Forest an opponent controls")
    void canTargetOpponentForest() {
        addCreatureReady(player1, new ArborElf());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Forest land")
    void cannotTargetNonForest() {
        addCreatureReady(player1, new ArborElf());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Forest");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ArborElf());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Can target an untapped Forest")
    void canTargetUntappedForest() {
        Permanent elf = addCreatureReady(player1, new ArborElf());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate an already tapped Arbor Elf")
    void cannotActivateTappedElf() {
        Permanent elf = addCreatureReady(player1, new ArborElf());
        elf.tap();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability still untaps its target after Arbor Elf leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent elf = addCreatureReady(player1, new ArborElf());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(elf);
        gd.playerGraveyards.get(player1.getId()).add(elf.getCard());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not untap another Forest when its target leaves")
    void doesNotRetargetWhenTargetLeaves() {
        Permanent elf = addCreatureReady(player1, new ArborElf());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent otherForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        otherForest.tap();

        harness.activateAbility(player1, 0, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerGraveyards.get(player1.getId()).add(forest.getCard());
        harness.passBothPriorities();

        assertThat(elf.isTapped()).isTrue();
        assertThat(otherForest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
