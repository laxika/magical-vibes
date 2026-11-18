package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauntlessVeteran.class, GrizzlyBears.class})
class DauntlessVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Dauntless Veteran boosts creatures you control")
    void attackBoostsControlledCreatures() {
        addCreatureReady(player1, new DauntlessVeteran());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, other)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauntless Veteran boosts itself when it attacks")
    void attackBoostsItself() {
        Permanent veteran = addCreatureReady(player1, new DauntlessVeteran());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dauntless Veteran does not boost an opponent's creatures")
    void opponentCreaturesNotBoosted() {
        addCreatureReady(player1, new DauntlessVeteran());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponent)).isEqualTo(0);
    }

    @Test
    @DisplayName("Dauntless Veteran's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DauntlessVeteran());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, other)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, other)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isEqualTo(0);
    }

    @Test
    @DisplayName("Each attacking Veteran contributes a separate boost")
    void multipleVeteransStackTheirBoosts() {
        Permanent first = addCreatureReady(player1, new DauntlessVeteran());
        Permanent second = addCreatureReady(player1, new DauntlessVeteran());
        Permanent nonattacker = addCreatureReady(player1, new DauntlessVeteran());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        for (Permanent creature : List.of(first, second, nonattacker)) {
            assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(2);
            assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive the boost")
    void creaturesPresentAtResolutionAreBoosted() {
        addCreatureReady(player1, new DauntlessVeteran());
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        Permanent newcomer = addCreatureReady(player1, new DauntlessVeteran());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, newcomer)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, newcomer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void laterCreaturesAreNotBoosted() {
        Permanent veteran = addCreatureReady(player1, new DauntlessVeteran());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent newcomer = addCreatureReady(player1, new DauntlessVeteran());

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, newcomer)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, newcomer)).isZero();
    }

    @Test
    @DisplayName("The attack trigger resolves even after its source leaves")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent veteran = addCreatureReady(player1, new DauntlessVeteran());
        Permanent other = addCreatureReady(player1, new DauntlessVeteran());
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(veteran);
        gd.playerGraveyards.get(player1.getId()).add(veteran.getCard());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, other)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isEqualTo(1);
    }
}
