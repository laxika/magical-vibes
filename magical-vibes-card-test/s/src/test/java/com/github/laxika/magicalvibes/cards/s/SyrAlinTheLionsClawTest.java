package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyrAlinTheLionsClaw.class, GrizzlyBears.class, Gingerbrute.class})
class SyrAlinTheLionsClawTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts other creatures you control until end of turn")
    void attackingBoostsOtherOwnCreatures() {
        Permanent syrAlin = addCreatureReady(player1, new SyrAlinTheLionsClaw());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, syrAlin)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, syrAlin)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger does not boost an opponent's creature")
    void doesNotBoostOpponentCreatures() {
        addCreatureReady(player1, new SyrAlinTheLionsClaw());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponent)).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SyrAlinTheLionsClaw());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
    }

    @Test
    @DisplayName("Creatures entering before the attack trigger resolves receive the boost")
    void boostsCreaturesPresentAtResolution() {
        addCreatureReady(player1, new SyrAlinTheLionsClaw());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new Gingerbrute());
        assertThat(PermanentGrants.powerBoostOf(gd, newcomer)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, newcomer)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, newcomer)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, newcomer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after the attack trigger resolves do not receive the boost")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        addCreatureReady(player1, new SyrAlinTheLionsClaw());
        Permanent existing = addCreatureReady(player1, new Gingerbrute());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new Gingerbrute());

        assertThat(PermanentGrants.powerBoostOf(gd, existing)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, existing)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, newcomer)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, newcomer)).isZero();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger Syr Alin")
    void doesNotTriggerWhenOnlyAnotherCreatureAttacks() {
        Permanent syrAlin = addCreatureReady(player1, new SyrAlinTheLionsClaw());
        Permanent attacker = addCreatureReady(player1, new Gingerbrute());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, syrAlin)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, syrAlin)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }
}
