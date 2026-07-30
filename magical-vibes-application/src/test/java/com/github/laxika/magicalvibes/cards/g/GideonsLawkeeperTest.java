package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GideonsLawkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps the target creature and taps the Lawkeeper as a cost")
    void resolvingTapsTarget() {
        Permanent lawkeeper = addReadyLawkeeper(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(lawkeeper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a creature its controller owns")
    void canTapOwnCreature() {
        addReadyLawkeeper(player1);
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, own.getId());
        harness.passBothPriorities();

        assertThat(own.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the ability twice in a turn because it requires tapping")
    void cannotActivateTwice() {
        addReadyLawkeeper(player1);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
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
        GideonsLawkeeper card = new GideonsLawkeeper();
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the ability without mana")
    void cannotActivateWithoutMana() {
        addReadyLawkeeper(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyLawkeeper(Player player) {
        Permanent perm = new Permanent(new GideonsLawkeeper());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
