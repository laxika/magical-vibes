package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.v.VampireOutcasts;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodlordOfVaasgoth.class, BloodSeeker.class, RuneclawBear.class, VampireOutcasts.class})
class BloodlordOfVaasgothTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 3: enters with three +1/+1 counters when an opponent was dealt damage")
    void ownBloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castBloodlord();

        assertThat(findPermanent(player1, "Bloodlord of Vaasgoth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodthirst 3: enters without counters when no opponent was dealt damage")
    void ownBloodthirstDoesNotApply() {
        castBloodlord();

        assertThat(findPermanent(player1, "Bloodlord of Vaasgoth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst ignores damage dealt only to its controller")
    void ownBloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castBloodlord();

        assertThat(findPermanent(player1, "Bloodlord of Vaasgoth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A cast Vampire creature spell gains bloodthirst 3 and enters with three counters")
    void grantsBloodthirstToVampireSpell() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.castFromHand(player1, new BloodSeeker(), "{1}{B}");
        resolveAllTriggers();

        Permanent vampire = findPermanent(player1, "Blood Seeker");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Granted bloodthirst does nothing when no opponent was dealt damage")
    void grantedBloodthirstInactiveWithoutDamage() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());

        harness.castFromHand(player1, new BloodSeeker(), "{1}{B}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Blood Seeker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A non-Vampire creature spell does not gain bloodthirst")
    void doesNotGrantBloodthirstToNonVampire() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Runeclaw Bear")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleBloodlordsGrantCumulativeBloodthirst() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());
        addCreatureReady(player1, new BloodlordOfVaasgoth());
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.castFromHand(player1, new BloodSeeker(), "{1}{B}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Blood Seeker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void grantedAndPrintedBloodthirstAccumulate() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.castFromHand(player1, new VampireOutcasts(), "{2}{B}{B}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Vampire Outcasts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void opponentBloodlordDoesNotGrantBloodthirst() {
        addCreatureReady(player2, new BloodlordOfVaasgoth());
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.castFromHand(player1, new BloodSeeker(), "{1}{B}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Blood Seeker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void damageAfterCastingStillEnablesBloodthirst() {
        addCreatureReady(player1, new BloodlordOfVaasgoth());
        harness.castFromHand(player1, new BloodSeeker(), "{1}{B}");
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Blood Seeker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void castBloodlord() {
        harness.castFromHand(player1, new BloodlordOfVaasgoth(), "{3}{B}{B}");
        resolveAllTriggers();
    }
}
