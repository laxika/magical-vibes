package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.e.Earthquake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SoulfireGrandMaster;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Damage one lifelink source deals to several recipients at once is a single life-gain event, so a
 * "whenever you gain life" ability triggers once for it; separate lifelink sources gain life separately.
 */
@CardUsed({AjanisPridemate.class, ArcTrail.class, Earthquake.class, GrizzlyBears.class, HillGiant.class,
        SoulfireGrandMaster.class, VampireNighthawk.class})
class LifelinkMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("A lifelink spell damaging every creature and player gains life once")
    void massDamageSpellGainsLifeOnce() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        Permanent pridemate = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1);

        // 1 damage to each of three creatures and two players: 20 - 1 + 5.
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A lifelink spell damaging a creature and a player as separate targets gains life once")
    void multiTargetSpellGainsLifeOnce() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        Permanent pridemate = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        UUID giantId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(giantId, player2.getId()));

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lifelink damage from a spell is gained before the controller's life total is checked")
    void spellLifelinkKeepsControllerAlive() {
        harness.setLife(player1, 1);
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        // 2 damage to each of two creatures and two players: 1 - 2 + 8.
        harness.assertLife(player1, 7);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A lifelink attacker dealing combat damage to two blockers gains life once")
    void combatDamageToSeveralBlockersGainsLifeOnce() {
        Permanent pridemate = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        Permanent blocker1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blocker2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(attackerIndex);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(attackerIndex);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, attackerIndex, Map.of(
                blocker1.getId(), 1,
                blocker2.getId(), 1));

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Completing the assignment lets the game run on and resolve Pridemate's single trigger.
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two lifelink attackers dealing combat damage gain life separately")
    void separateLifelinkAttackersGainLifeSeparately() {
        Permanent pridemate = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        Permanent attacker1 = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        Permanent attacker2 = harness.addToBattlefieldAndReturn(player1, new VampireNighthawk());
        for (Permanent attacker : List.of(attacker1, attacker2)) {
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
