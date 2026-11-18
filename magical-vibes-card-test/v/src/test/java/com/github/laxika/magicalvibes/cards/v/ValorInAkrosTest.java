package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Valor in Akros")
@CardUsed({ValorInAkros.class, GrizzlyBears.class})
class ValorInAkrosTest extends BaseCardTest {

    private void castBears(Player player) {
        harness.castFromHand(player, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature you control entering pumps your team")
    void ownCreatureEnterPumpsTeam() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBears(player1);
        harness.passBothPriorities(); // resolve the trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent entered = battlefield.get(battlefield.size() - 1);

        assertThat(PermanentGrants.powerBoostOf(gd, existing)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, existing)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, entered)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, entered)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBears(player1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, existing)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, existing)).isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        castBears(player2);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, existing)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, existing)).isEqualTo(0);
    }

    @Test
    @DisplayName("Successive entries give separate boosts without granting old boosts to new creatures")
    void successiveEntriesAccumulate() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent first = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(PermanentGrants.powerBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, existing)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, existing)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponent)).isZero();
    }

    @Test
    @DisplayName("A pending trigger boosts creatures that enter before it resolves")
    void creaturesAreChosenOnResolution() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(2);
    }
}
