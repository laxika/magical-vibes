package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TawnossWandTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes a power-2 creature unblockable this turn")
    void makesTargetUnblockable() {
        Permanent wand = addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(wand.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addReadyWand(player1);
        Permanent bigCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bigCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the {2} mana cost")
    void cannotActivateWithoutMana() {
        addReadyWand(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWand(Player player) {
        Permanent perm = new Permanent(new TawnossWand());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
