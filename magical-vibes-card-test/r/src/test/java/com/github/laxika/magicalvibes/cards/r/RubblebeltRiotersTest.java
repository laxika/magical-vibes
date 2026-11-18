package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SamutsSprint;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RubblebeltRioters.class, HillGiant.class, SamutsSprint.class})
class RubblebeltRiotersTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 based on the greatest power among creatures you control")
    void boostsByGreatestControlledPower() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, rioters)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, rioters)).isZero();
    }

    @Test
    @DisplayName("Opponent creatures do not contribute to the boost")
    void ignoresOpponentCreatures() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, rioters)).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, rioters)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, rioters)).isZero();
    }

    @Test
    @DisplayName("Uses its own power at resolution and keeps the resolved boost fixed")
    void usesCurrentPowerAndDoesNotRecalculateAfterResolution() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SamutsSprint(), new SamutsSprint()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);

            harness.castAndResolveInstant(player1, 0, rioters.getId());
            resolveAllTriggers();

            assertThat(PermanentGrants.powerBoostOf(gd, rioters)).isEqualTo(4);
            assertThat(PermanentGrants.toughnessBoostOf(gd, rioters)).isEqualTo(1);

            harness.castAndResolveInstant(player1, 0, rioters.getId());

            assertThat(PermanentGrants.powerBoostOf(gd, rioters)).isEqualTo(6);
            assertThat(PermanentGrants.toughnessBoostOf(gd, rioters)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Uses the greatest power, not the sum, including nonattacking creatures")
    void countsNonattackingCreaturesAndUsesMaximumPower() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        var smaller = addCreatureReady(player1, new RubblebeltRioters());
        var greatest = addCreatureReady(player1, new RubblebeltRioters());
        PermanentGrants.setPowerBoost(gd, smaller, 2);
        PermanentGrants.setPowerBoost(gd, greatest, 5);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, rioters)).isEqualTo(5);
        assertThat(PermanentGrants.powerBoostOf(gd, smaller)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, greatest)).isEqualTo(5);
    }

    @Test
    @DisplayName("Each simultaneous attack trigger evaluates power independently as it resolves")
    void simultaneousAttackTriggersUseEarlierResolvedBoosts() {
        var first = addCreatureReady(player1, new RubblebeltRioters());
        var second = addCreatureReady(player1, new RubblebeltRioters());
        PermanentGrants.setPowerBoost(gd, first, 2);
        PermanentGrants.setPowerBoost(gd, second, 2);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(List.of(PermanentGrants.powerBoostOf(gd, first), PermanentGrants.powerBoostOf(gd, second)))
                .containsExactlyInAnyOrder(4, 6);
    }

    @Test
    @DisplayName("A summoning-sick Rioters can attack and trigger because it has haste")
    void hasteAllowsAttackAndTriggerOnArrivalTurn() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        rioters.setSummoningSick(true);
        PermanentGrants.setPowerBoost(gd, rioters, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, rioters)).isEqualTo(4);
    }
}
