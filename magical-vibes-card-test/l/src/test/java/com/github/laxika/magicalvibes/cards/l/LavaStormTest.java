package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArdentMilitia;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.s.SoulSculptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavaStorm.class, BenalishKnight.class, ArdentMilitia.class, SoulSculptor.class})
class LavaStormTest extends BaseCardTest {

    private void setUpCombat() {
        addCreatureReady(player1, new BenalishKnight());
        addCreatureReady(player2, new BenalishKnight());
        addCreatureReady(player2, new ArdentMilitia());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void castLavaStorm(int modeIndex) {
        castLavaStormWithoutResolving(modeIndex);
        harness.passBothPriorities();
    }

    private void castLavaStormWithoutResolving(int modeIndex) {
        harness.setHand(player1, List.of(new LavaStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castModalInstant(player1, 0, modeIndex, List.of());
    }

    @Test
    @DisplayName("Attacking mode deals 2 damage to each attacking creature only")
    void attackingModeHitsAttackers() {
        setUpCombat();

        castLavaStorm(0);

        harness.assertNotOnBattlefield(player1, "Benalish Knight");
        harness.assertOnBattlefield(player2, "Benalish Knight");
        assertThat(findPermanent(player2, "Ardent Militia").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Blocking mode deals 2 damage to each blocking creature only")
    void blockingModeHitsBlockers() {
        setUpCombat();

        castLavaStorm(1);

        harness.assertOnBattlefield(player1, "Benalish Knight");
        harness.assertNotOnBattlefield(player2, "Benalish Knight");
        assertThat(findPermanent(player2, "Ardent Militia").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking mode deals exactly 2 damage to an attacking creature")
    void attackingModeDealsExactlyTwoDamage() {
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        castLavaStorm(0);

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocking mode deals exactly 2 damage to a blocking creature")
    void blockingModeDealsExactlyTwoDamage() {
        addCreatureReady(player1, new BenalishKnight());
        Permanent blocker = addCreatureReady(player2, new ArdentMilitia());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castLavaStorm(1);

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Outside combat neither mode damages anything")
    void noCombatNoDamage(int modeIndex) {
        addCreatureReady(player1, new BenalishKnight());

        castLavaStorm(modeIndex);

        harness.assertOnBattlefield(player1, "Benalish Knight");
        assertThat(findPermanent(player1, "Benalish Knight").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Lava Storm");
    }

    @Test
    @CardUsed(SoulSculptor.class)
    @DisplayName("Does not damage an attacking permanent that is no longer a creature")
    void doesNotDamageAttackingPermanentThatStopsBeingCreature() {
        addCreatureReady(player1, new SoulSculptor());
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());
        addCreatureReady(player2, new BenalishKnight());
        declareAttackers(List.of(1));
        assertThat(attacker.isAttacking()).isTrue();

        castLavaStormWithoutResolving(0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, attacker)).isFalse();
        assertThat(gd.currentStep).isIn(TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS,
                TurnStep.COMBAT_DAMAGE, TurnStep.END_OF_COMBAT);
        assertThat(attacker.isAttacking()).isFalse();

        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking mode damages every attacker and leaves nonattackers unharmed")
    void attackingModeHitsMultipleAttackers() {
        Permanent first = addCreatureReady(player1, new ArdentMilitia());
        Permanent second = addCreatureReady(player1, new ArdentMilitia());
        Permanent idle = addCreatureReady(player1, new ArdentMilitia());
        Permanent defender = addCreatureReady(player2, new ArdentMilitia());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castLavaStorm(0);

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        assertThat(idle.getMarkedDamage()).isZero();
        assertThat(defender.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blocking mode damages every blocker and leaves nonblockers unharmed")
    void blockingModeHitsMultipleBlockers() {
        Permanent attacker = addCreatureReady(player1, new ArdentMilitia());
        Permanent first = addCreatureReady(player2, new ArdentMilitia());
        Permanent second = addCreatureReady(player2, new ArdentMilitia());
        Permanent idle = addCreatureReady(player2, new ArdentMilitia());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        castLavaStorm(1);

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
        assertThat(idle.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
