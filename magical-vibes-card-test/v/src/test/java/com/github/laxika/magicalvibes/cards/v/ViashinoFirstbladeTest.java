package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViashinoFirstblade.class})
class ViashinoFirstbladeTest extends BaseCardTest {

    private Permanent castFirstblade(Player player) {
        harness.castFromHand(player, new ViashinoFirstblade(), "{1}{R}{W}");
        resolveAllTriggers();
        return findPermanent(player, "Viashino Firstblade");
    }

    @Test
    @DisplayName("Gets +2/+2 after its enter trigger resolves")
    void etbBoostsSelf() {
        Permanent firstblade = castFirstblade(player1);

        assertThat(gqs.getEffectivePower(gd, firstblade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstblade)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn, leaving a 2/2")
    void boostWearsOff() {
        Permanent firstblade = castFirstblade(player1);
        assertThat(gqs.getEffectivePower(gd, firstblade)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstblade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstblade)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can attack the turn it is cast")
    void canAttackTheTurnItIsCast() {
        Permanent firstblade = castFirstblade(player1);

        assertThat(firstblade.isSummoningSick()).isTrue();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(firstblade.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, firstblade)).isEqualTo(4);
    }

    @Test
    @DisplayName("Entry without casting queues a boost that affects only the entering creature")
    void noncastEntryBoostsOnlyItsSourceAfterResolution() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ViashinoFirstblade());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ViashinoFirstblade());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new ViashinoFirstblade());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(2);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(2);
    }
}
