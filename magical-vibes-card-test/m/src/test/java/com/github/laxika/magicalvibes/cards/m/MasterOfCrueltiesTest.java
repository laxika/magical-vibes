package com.github.laxika.magicalvibes.cards.m;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.cards.b.BorosMastiff;
import com.github.laxika.magicalvibes.cards.r.RalZarek;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterOfCruelties.class, BorosMastiff.class, RalZarek.class, Skullcrack.class})
class MasterOfCrueltiesTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking unblocked sets the defending player's life to 1 and prevents its combat damage")
    void unblockedSetsLifeToOneAndAssignsNoDamage() {
        Permanent master = addAttackingMaster(player1, player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(master.getId());
    }

    @Test
    @DisplayName("Being blocked leaves the defending player's life alone")
    void blockedDoesNotSetLife() {
        Permanent master = addAttackingMaster(player1, player2);
        addCreatureReady(player2, new BorosMastiff());
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(master.getId());
    }

    @Test
    @DisplayName("Can be declared as the sole attacker")
    void attacksAlone() {
        addCreatureReady(player1, new MasterOfCruelties());
        addCreatureReady(player1, new BorosMastiff());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can't be declared as an attacker alongside another creature")
    void cannotAttackWithOthers() {
        addCreatureReady(player1, new MasterOfCruelties());
        addCreatureReady(player1, new BorosMastiff());

        assertThatThrownBy(() -> declareAttackers(List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only attack alone");
    }

    @Test
    @DisplayName("Attacking a planeswalker does not change its controller's life")
    void attackingPlaneswalkerDoesNotTrigger() {
        Permanent master = addAttackingMaster(player1, player2);
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalZarek());
        ral.setCounterCount(CounterType.LOYALTY, 4);
        master.setAttackTarget(ral.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("First strike and deathtouch kill a blocker before it deals damage")
    void blockedMasterKillsBlockerWithoutTakingDamage() {
        Permanent master = addCreatureReady(player1, new MasterOfCruelties());
        addCreatureReady(player2, new BorosMastiff());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Boros Mastiff");
        assertThat(master.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The defending player is affected when the second player attacks")
    void secondPlayerAttackingAffectsFirstPlayer() {
        addAttackingMaster(player2, player1);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();
        resolveCombat(player2);

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unpreventable damage does not bypass assigning no combat damage")
    void unpreventableDamageStillNotAssigned() {
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        addAttackingMaster(player1, player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveCombat();

        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("Damage suppression expires before a later combat in the same turn")
    void canDealDamageWhenBlockedInAdditionalCombat() {
        Permanent master = addAttackingMaster(player1, player2);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveCombat();
        harness.assertLife(player2, 1);

        gd.additionalCombatPhasesOnly = 1;
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);
        master.untap();
        addCreatureReady(player2, new BorosMastiff());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Boros Mastiff");
        harness.assertOnBattlefield(player1, "Master of Cruelties");
        harness.assertLife(player2, 1);
    }

    private Permanent addAttackingMaster(Player attacker, Player defender) {
        Permanent perm = addCreatureReady(attacker, new MasterOfCruelties());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
