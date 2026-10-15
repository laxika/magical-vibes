package com.github.laxika.magicalvibes.cards.v;

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

        assertThat(existing.getPowerModifier()).isEqualTo(1);
        assertThat(existing.getToughnessModifier()).isEqualTo(1);
        assertThat(entered.getPowerModifier()).isEqualTo(1);
        assertThat(entered.getToughnessModifier()).isEqualTo(1);
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

        assertThat(existing.getPowerModifier()).isEqualTo(0);
        assertThat(existing.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        castBears(player2);
        harness.passBothPriorities();

        assertThat(existing.getPowerModifier()).isEqualTo(0);
        assertThat(existing.getToughnessModifier()).isEqualTo(0);
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

        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        harness.passBothPriorities();

        assertThat(existing.getPowerModifier()).isEqualTo(2);
        assertThat(existing.getToughnessModifier()).isEqualTo(2);
        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A pending trigger boosts creatures that enter before it resolves")
    void creaturesAreChosenOnResolution() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(first.getPowerModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(2);
    }
}
