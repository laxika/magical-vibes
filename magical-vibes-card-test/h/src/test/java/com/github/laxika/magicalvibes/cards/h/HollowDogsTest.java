package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowDogs.class})
class HollowDogsTest extends BaseCardTest {

    @Test
    @DisplayName("Attack boost waits for the triggered ability to resolve")
    void boostWaitsForTriggerResolution() {
        Permanent dogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));

        assertThat(PermanentGrants.powerBoostOf(gd, dogs)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, dogs)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dogs)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dogs)).isZero();
    }

    @Test
    @DisplayName("Two attacking Hollow Dogs each receive only their own boost")
    void simultaneousAttackersEachGetTheirOwnBoost() {
        Permanent firstDogs = addCreatureReady(player1, new HollowDogs());
        Permanent secondDogs = addCreatureReady(player1, new HollowDogs());
        Permanent defendingDogs = addCreatureReady(player2, new HollowDogs());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, firstDogs)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstDogs)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, secondDogs)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondDogs)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, defendingDogs)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, defendingDogs)).isZero();
    }

    @Test
    @DisplayName("Attacking puts the attack trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard() instanceof HollowDogs);
    }

    @Test
    @DisplayName("Attacking puts the attack trigger on the stack")
    void attackPutsTriggerOnStackUpstreamReview() {
        Permanent dogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && dogs.getId().equals(e.getSourcePermanentId()));
    }

    @Test
    @DisplayName("Only the attacking Hollow Dogs gets the boost")
    void onlyAttackingDogGetsBoost() {
        Permanent attackingDogs = addCreatureReady(player1, new HollowDogs());
        Permanent restingDogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attackingDogs)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, restingDogs)).isZero();
    }

    @Test
    @DisplayName("Gets +2/+0 until end of turn when it attacks")
    void getsBoostWhenAttacking() {
        Permanent dogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dogs)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dogs)).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent dogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dogs)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(PermanentGrants.powerBoostOf(gd, dogs)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dogs)).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurnUpstreamReview() {
        Permanent dogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dogs)).isEqualTo(2);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(PermanentGrants.powerBoostOf(gd, dogs)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dogs)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the attacking creature gets the attack boost")
    void onlyAttackingCreatureGetsBoost() {
        Permanent attackingDogs = addCreatureReady(player1, new HollowDogs());
        Permanent restingDogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attackingDogs)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, restingDogs)).isEqualTo(0);
    }
}
