package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootKinAlly.class, GrizzlyBears.class})
class RootKinAllyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping itself and another creature gives Root-Kin Ally +2/+2")
    void tappingItselfAndAnotherCreatureBoostsIt() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(5);
        assertThat(ally.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller chooses which two creatures to tap")
    void choosesTwoCreaturesToTap() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent spare = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(5);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(spare.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, ally), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability requires two untapped creatures you control")
    void requiresTwoUntappedCreatures() {
        Permanent ally = addCreatureReady(player1, new RootKinAlly());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ally), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
