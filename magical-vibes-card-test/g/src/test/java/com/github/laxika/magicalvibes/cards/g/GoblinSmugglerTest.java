package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinSmuggler.class, GrizzlyBears.class, HillGiant.class})
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

    @Test
    @DisplayName("Haste allows immediate activation and tapping pays the cost")
    void hasteAllowsImmediateActivation() {
        Permanent smuggler = harness.addToBattlefieldAndReturn(player1, new GoblinSmuggler());
        smuggler.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoblinSmuggler());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(smuggler.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A target whose power increases above two before resolution becomes illegal")
    void increasedPowerBeforeResolutionInvalidatesTarget() {
        addReadySmuggler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoblinSmuggler());

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Power increases after resolution do not remove unblockability")
    void increasedPowerAfterResolutionDoesNotRemoveEffect() {
        addReadySmuggler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoblinSmuggler());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Ability resolves even if its source leaves the battlefield")
    void abilitySurvivesSourceLeavingBattlefield() {
        Permanent smuggler = addReadySmuggler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoblinSmuggler());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(smuggler);
        gd.playerGraveyards.get(player1.getId()).add(smuggler.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isTrue();
    }
    private Permanent addReadySmuggler(Player player) {
        return addReady(new GoblinSmuggler(), player);
    }

    private Permanent addReady(Card card, Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
