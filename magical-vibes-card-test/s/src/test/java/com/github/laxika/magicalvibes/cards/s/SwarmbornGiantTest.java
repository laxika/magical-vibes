package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmbornGiant.class, SatyrGrovedancer.class, MagmaSpray.class})
class SwarmbornGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts two +1/+1 counters on Swarmborn Giant and gives it reach")
    void monstrosityAddsCountersAndReach() {
        Permanent giant = addReadyGiant(player1);
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.isMonstrous()).isTrue();
        assertThat(giant.getEffectivePower()).isEqualTo(8);
        assertThat(giant.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Monstrosity can be activated again but has no effect on a monstrous Giant")
    void monstrosityOnlyResolvesOnce() {
        Permanent giant = addReadyGiant(player1);
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Combat damage to Swarmborn Giant alone does not cause sacrifice")
    void combatDamageToGiantDoesNotCauseSacrifice() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SatyrGrovedancer());
        Permanent giant = addReadyGiant(player2);

        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        giant.setBlocking(true);
        giant.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Swarmborn Giant");
        harness.assertNotInGraveyard(player2, "Swarmborn Giant");
    }

    @Test
    @DisplayName("Swarmborn Giant is sacrificed when its controller is dealt combat damage")
    void combatDamageToControllerCausesSacrifice() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SatyrGrovedancer());
        addReadyGiant(player2);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Swarmborn Giant");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Swarmborn Giant");
        harness.assertInGraveyard(player2, "Swarmborn Giant");
    }

    @Test
    @DisplayName("Combat damage to an opponent does not sacrifice Swarmborn Giant")
    void combatDamageToOpponentDoesNotCauseSacrifice() {
        Permanent giant = addReadyGiant(player1);
        giant.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertOnBattlefield(player1, "Swarmborn Giant");
        harness.assertNotInGraveyard(player1, "Swarmborn Giant");
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Swarmborn Giant's sacrifice ability")
    void nonCombatDamageDoesNotCauseSacrifice() {
        Permanent giant = addReadyGiant(player2);
        harness.setHand(player1, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        harness.assertOnBattlefield(player2, "Swarmborn Giant");
    }

    @Test
    @DisplayName("Two pending monstrosity abilities add counters only once")
    void multiplePendingMonstrosityAbilities() {
        Permanent giant = addReadyGiant(player1);
        addMonstrosityMana(player1);
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("An ordinary +1/+1 counter does not make the Giant monstrous or grant reach")
    void ordinaryCounterDoesNotGrantReach() {
        Permanent giant = addReadyGiant(player1);
        harness.setHand(player1, List.of(new SatyrGrovedancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(giant.isMonstrous()).isFalse();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.REACH)).isFalse();
    }

    private Permanent addReadyGiant(Player player) {
        Permanent giant = harness.addToBattlefieldAndReturn(player, new SwarmbornGiant());
        giant.setSummoningSick(false);
        return giant;
    }

    private void addMonstrosityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.addMana(player, ManaColor.GREEN, 2);
    }
}
