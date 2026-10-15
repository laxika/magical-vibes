package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinotaurSkullcleaver.class})
class MinotaurSkullcleaverTest extends BaseCardTest {

    private Permanent castSkullcleaver(Player player) {
        harness.castFromHand(player, new MinotaurSkullcleaver(), "{2}{R}");
        resolveAllTriggers();
        return findPermanent(player, "Minotaur Skullcleaver");
    }

    @Test
    @DisplayName("Enters as a 4/2 thanks to the +2/+0 ETB boost")
    void etbBoostsSelf() {
        Permanent skullcleaver = castSkullcleaver(player1);

        assertThat(gqs.getEffectivePower(gd, skullcleaver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skullcleaver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn, leaving a 2/2")
    void boostWearsOff() {
        Permanent skullcleaver = castSkullcleaver(player1);
        assertThat(gqs.getEffectivePower(gd, skullcleaver)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skullcleaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skullcleaver)).isEqualTo(2);
    }

    @Test
    @DisplayName("The entry boost waits for its trigger to resolve and affects only its source")
    void boostUsesStackAndOnlyAffectsSource() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MinotaurSkullcleaver());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new MinotaurSkullcleaver());
        harness.castFromHand(player1, new MinotaurSkullcleaver(), "{2}{R}");
        harness.passBothPriorities();
        Permanent entering = findPermanents(player1, "Minotaur Skullcleaver").stream()
                .filter(permanent -> !permanent.getId().equals(other.getId()))
                .findFirst().orElseThrow();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(2);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can attack on the turn it enters and deals boosted combat damage")
    void hasteAllowsBoostedAttack() {
        castSkullcleaver(player1);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The boost remains during the end step before cleanup")
    void boostLastsThroughEndStep() {
        Permanent skullcleaver = castSkullcleaver(player1);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.getEffectivePower(gd, skullcleaver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skullcleaver)).isEqualTo(2);
    }
}
