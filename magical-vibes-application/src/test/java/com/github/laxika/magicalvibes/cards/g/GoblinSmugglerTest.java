package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinSmugglerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability makes another creature with power 2 or less unblockable")
    void resolvingMakesTargetUnblockable() {
        addReadySmuggler(player1);
        Permanent target = addReady(new GrizzlyBears(), player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot target Goblin Smuggler itself")
    void cannotTargetItself() {
        Permanent smuggler = addReadySmuggler(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, smuggler.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addReadySmuggler(player1);
        Permanent giant = addReady(new HillGiant(), player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblockable resets at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        addReadySmuggler(player1);
        Permanent target = addReady(new GrizzlyBears(), player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    private Permanent addReadySmuggler(Player player) {
        return addReady(new GoblinSmuggler(), player);
    }

    private Permanent addReady(Card card, Player player) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
