package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.c.CabalTorturer;
import com.github.laxika.magicalvibes.cards.m.MortalCombat;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanConstrictor.class, CabalTorturer.class, BaskingRootwalla.class,
        MortalCombat.class, Swamp.class})
class KrosanConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it gives a black creature -2/-0 until end of turn")
    void weakensTargetBlackCreatureUntilEndOfTurn() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        Permanent skeletons = addCreatureReady(player2, new CabalTorturer());
        int originalPower = gqs.getEffectivePower(gd, skeletons);
        int originalToughness = gqs.getEffectiveToughness(gd, skeletons);

        harness.activateAbility(player1, 0, null, skeletons.getId());
        harness.passBothPriorities();

        assertThat(constrictor.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(originalPower - 2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(originalToughness);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("Cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        Permanent bears = addCreatureReady(player1, new BaskingRootwalla());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
        assertThat(constrictor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a black noncreature permanent")
    void cannotTargetBlackNoncreaturePermanent() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        Permanent mortalCombat = harness.addToBattlefieldAndReturn(player2, new MortalCombat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mortalCombat.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(constrictor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Swampwalk prevents blocking while the defending player controls a Swamp")
    void swampwalkPreventsBlockingWithDefendingSwamp() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        constrictor.setAttacking(true);
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new BaskingRootwalla());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(constrictor);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Swampwalk does not prevent blocking when the defending player controls no Swamp")
    void swampwalkAllowsBlockingWithoutDefendingSwamp() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        constrictor.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BaskingRootwalla());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(constrictor);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.getBlockingTargets()).containsExactly(attackerIndex);
    }

    @Test
    @DisplayName("Can weaken a black creature controlled by its controller")
    void canTargetFriendlyBlackCreature() {
        addCreatureReady(player1, new KrosanConstrictor());
        Permanent target = addCreatureReady(player1, new CabalTorturer());
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower - 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("A summoning-sick constrictor cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent constrictor = harness.addToBattlefieldAndReturn(player1, new KrosanConstrictor());
        constrictor.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new CabalTorturer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(constrictor.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped constrictor cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        constrictor.tap();
        Permanent target = addCreatureReady(player2, new CabalTorturer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        Permanent target = addCreatureReady(player2, new CabalTorturer());
        int originalPower = gqs.getEffectivePower(gd, target);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(constrictor);
        gd.playerGraveyards.get(player1.getId()).add(constrictor.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower - 2);
    }

    @Test
    @DisplayName("The ability does not affect another creature when its target leaves")
    void abilityDoesNotRetargetAfterTargetLeaves() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        Permanent target = addCreatureReady(player2, new CabalTorturer());
        Permanent otherCreature = addCreatureReady(player2, new CabalTorturer());
        int originalPower = gqs.getEffectivePower(gd, otherCreature);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(constrictor.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(originalPower);
    }

    @Test
    @DisplayName("A Swamp controlled only by the attacker does not prevent blocking")
    void attackersSwampDoesNotEnableSwampwalk() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        constrictor.setAttacking(true);
        harness.addToBattlefield(player1, new Swamp());
        Permanent blocker = addCreatureReady(player2, new BaskingRootwalla());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(constrictor);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.getBlockingTargets()).containsExactly(attackerIndex);
    }
}
