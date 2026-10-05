package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({MacCreadyLamplightMayor.class, GrizzlyBears.class, HillGiant.class, AirElemental.class})
class MacCreadyLamplightMayorTest extends BaseCardTest {

    @Test
    @DisplayName("A small attacking creature gains skulk until end of turn")
    void smallAttackerGainsSkulk() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.SKULK)).isTrue();
    }

    @Test
    @DisplayName("The skulk trigger does not trigger for a creature with power 3")
    void skulkTriggerRequiresPowerTwoOrLess() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with power 4 or greater attacking directly drains its controller")
    void largeAttackerDrainsItsController() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        addCreatureReady(player2, new AirElemental());
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 2);
    }

    @Test
    @DisplayName("The drain trigger does not trigger for a creature with power 3")
    void drainTriggerRequiresPowerFourOrGreater() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("MacCready and each other qualifying attacker gain skulk independently")
    void eachSmallAttackerGainsSkulk() {
        Permanent mayor = addCreatureReady(player1, new MacCreadyLamplightMayor());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        declareAttackers(player1, List.of(0, 1, 2));
        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gqs.hasKeyword(gd, mayor, Keyword.SKULK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.SKULK)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.SKULK)).isFalse();
    }

    @Test
    @DisplayName("The skulk trigger does not recheck power at resolution and expires at cleanup")
    void skulkPersistsAfterPowerIncreaseUntilCleanup() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        attacker.setPowerModifier(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.SKULK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.SKULK)).isFalse();
    }

    @Test
    @DisplayName("Granted skulk forbids a greater-power blocker but allows an equal-power blocker")
    void grantedSkulkRestrictsBlocking() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        Permanent equalBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 1)));
        assertThat(equalBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Each large attacker drains separately even if its power later decreases")
    void eachLargeAttackerDrainsWithoutRecheckingPower() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        Permanent first = addCreatureReady(player2, new AirElemental());
        addCreatureReady(player2, new AirElemental());
        int defenderLife = gd.getLife(player1.getId());
        int attackerLife = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        first.setPowerModifier(-2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player1.getId())).isEqualTo(defenderLife + 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(attackerLife - 4);
    }

    @Test
    @DisplayName("The drain still resolves after both MacCready and the attacker leave")
    void drainResolvesAfterSourceAndAttackerLeave() {
        Permanent mayor = addCreatureReady(player1, new MacCreadyLamplightMayor());
        Permanent attacker = addCreatureReady(player2, new AirElemental());
        int defenderLife = gd.getLife(player1.getId());
        int attackerLife = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(mayor);
        gd.playerGraveyards.get(player1.getId()).add(mayor.getCard());
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        gd.playerGraveyards.get(player2.getId()).add(attacker.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player1.getId())).isEqualTo(defenderLife + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(attackerLife - 2);
    }

    @Test
    @DisplayName("An opposing small attacker does not receive skulk")
    void opposingSmallAttackerDoesNotGainSkulk() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.SKULK)).isFalse();
    }

    @Test
    @DisplayName("The drain threshold uses current power when attackers are declared")
    void drainUsesModifiedPowerAtTriggerTime() {
        addCreatureReady(player1, new MacCreadyLamplightMayor());
        Permanent giant = addCreatureReady(player2, new HillGiant());
        giant.setPowerModifier(1);
        Permanent elemental = addCreatureReady(player2, new AirElemental());
        elemental.setPowerModifier(-1);
        int defenderLife = gd.getLife(player1.getId());
        int attackerLife = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player1.getId())).isEqualTo(defenderLife + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(attackerLife - 2);
    }
}
