package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosTrumpeter.class})
class RakdosTrumpeterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Rakdos Trumpeter +2/+0 until end of turn")
    void activationBoostsSelf() {
        Permanent trumpeter = addCreatureReady(player1, new RakdosTrumpeter());
        int basePower = gqs.getEffectivePower(gd, trumpeter);
        int baseToughness = gqs.getEffectiveToughness(gd, trumpeter);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, trumpeter)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent trumpeter = addCreatureReady(player1, new RakdosTrumpeter());
        int basePower = gqs.getEffectivePower(gd, trumpeter);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower);
    }

    @Test
    void repeatedActivationsStackAndOnlyBoostTheirSource() {
        Permanent trumpeter = addCreatureReady(player1, new RakdosTrumpeter());
        Permanent other = addCreatureReady(player1, new RakdosTrumpeter());
        int basePower = gqs.getEffectivePower(gd, trumpeter);
        int otherPower = gqs.getEffectivePower(gd, other);
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(otherPower);
        assertThat(trumpeter.isTapped()).isFalse();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent trumpeter = harness.addToBattlefieldAndReturn(player1, new RakdosTrumpeter());
        trumpeter.setSummoningSick(true);
        trumpeter.setTapped(true);
        int basePower = gqs.getEffectivePower(gd, trumpeter);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, trumpeter)).isEqualTo(basePower + 2);
        assertThat(trumpeter.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new RakdosTrumpeter());
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new RakdosTrumpeter());
        addCreatureReady(player2, new RakdosTrumpeter());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new RakdosTrumpeter());
        Permanent first = addCreatureReady(player2, new RakdosTrumpeter());
        Permanent second = addCreatureReady(player2, new RakdosTrumpeter());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}