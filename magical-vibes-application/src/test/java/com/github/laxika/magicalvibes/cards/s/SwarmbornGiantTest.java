package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Swarmborn Giant's monstrosity ability can resolve only once")
    void monstrosityOnlyResolvesOnce() {
        addReadyGiant(player1);
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    @Test
    @DisplayName("Swarmborn Giant sacrifices itself when dealt combat damage")
    void combatDamageCausesSacrifice() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent giant = addReadyGiant(player2);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        giant.setBlocking(true);
        giant.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Swarmborn Giant");
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Swarmborn Giant's sacrifice ability")
    void nonCombatDamageDoesNotCauseSacrifice() {
        Permanent giant = addReadyGiant(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Swarmborn Giant");
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
