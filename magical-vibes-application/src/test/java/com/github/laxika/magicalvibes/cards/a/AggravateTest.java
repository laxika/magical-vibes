package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EverlastingTorment;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbbeyGargoyles.class, Aggravate.class, EverlastingTorment.class, GrizzlyBears.class, JayemdaeTome.class})
class AggravateTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature the target player controls and forces them to attack")
    void damagesAndForcesAttack() {
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherEnemyBear = addCreatureReady(player2, new GrizzlyBears());

        castAggravate(player2.getId());

        assertThat(enemyBear.getMarkedDamage()).isEqualTo(1);
        assertThat(otherEnemyBear.getMarkedDamage()).isEqualTo(1);
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
        assertThat(otherEnemyBear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Leaves the caster's creatures and the target's non-creatures untouched")
    void doesNotAffectCasterCreaturesOrNonCreatures() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new JayemdaeTome());
        Permanent enemyArtifact = gd.playerBattlefields.get(player2.getId()).getFirst();

        castAggravate(player2.getId());

        assertThat(ownBear.getMarkedDamage()).isZero();
        assertThat(ownBear.isMustAttackThisTurn()).isFalse();
        assertThat(enemyArtifact.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature with protection from red is neither damaged nor forced to attack")
    void protectedCreatureIsNotDamagedOrForced() {
        Permanent gargoyles = addCreatureReady(player2, new AbbeyGargoyles());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        castAggravate(player2.getId());

        assertThat(gargoyles.getMarkedDamage()).isZero();
        assertThat(gargoyles.isMustAttackThisTurn()).isFalse();
        // The unprotected creature is still damaged and forced.
        assertThat(enemyBear.getMarkedDamage()).isEqualTo(1);
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The must-attack requirement forces an attack during the target player's combat")
    void mustAttackRequirementForcesAttackInCombat() {
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castAggravate(player2.getId());
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        List<Integer> attackable = harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId());
        assertThat(harness.getCombatAttackService()
                .getMustAttackIndices(gd, player2.getId(), attackable)).contains(0);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target its own controller, hitting the caster's own creatures")
    void canTargetOwnController() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());

        castAggravate(player1.getId());

        assertThat(ownBear.getMarkedDamage()).isEqualTo(1);
        assertThat(ownBear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Damage dealt as -1/-1 counters still requires the damaged creature to attack")
    void counterDamageStillForcesAttack() {
        harness.addToBattlefield(player1, new EverlastingTorment());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        castAggravate(player2.getId());

        assertThat(enemyBear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(enemyBear.getMarkedDamage()).isZero();
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Tapped and summoning-sick creatures take damage but cannot be required to attack")
    void creaturesUnableToAttackCanStayBack() {
        Permanent tappedBear = addCreatureReady(player2, new GrizzlyBears());
        tappedBear.setTapped(true);
        Permanent sickBear = addCreatureReady(player2, new GrizzlyBears());
        sickBear.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castAggravate(player2.getId());

        assertThat(tappedBear.getMarkedDamage()).isEqualTo(1);
        assertThat(sickBear.getMarkedDamage()).isEqualTo(1);
        declareAttackers(player2, List.of());
    }

    @Test
    @DisplayName("Creatures entering after resolution are not required to attack")
    void laterCreatureIsUnaffected() {
        castAggravate(player2.getId());

        Permanent laterBear = addCreatureReady(player2, new GrizzlyBears());

        assertThat(laterBear.getMarkedDamage()).isZero();
        assertThat(laterBear.isMustAttackThisTurn()).isFalse();
    }

    private void castAggravate(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Aggravate()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
