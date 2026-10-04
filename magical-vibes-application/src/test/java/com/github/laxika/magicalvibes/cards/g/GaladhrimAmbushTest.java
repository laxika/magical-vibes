package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaladhrimAmbush.class, GrizzlyBears.class, ElvishWarrior.class})
class GaladhrimAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Elf Warrior token per attacking creature across all players")
    void createsTokenPerAttackingCreature() {
        Permanent firstAttacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent thirdAttacker = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        thirdAttacker.setAttacking(true);

        castGaladhrimAmbush();

        assertThat(elfWarriorTokenCount(player1)).isEqualTo(3);
        assertThat(elfWarriorTokenCount(player2)).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage from non-Elves but not from Elves")
    void preventsCombatDamageFromNonElves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        castGaladhrimAmbush();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, elf, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, false)).isFalse();
    }

    @Test
    @DisplayName("No attackers creates no tokens but still prevents damage from later creatures")
    void noAttackersStillEstablishesPrevention() {
        castGaladhrimAmbush();

        assertThat(elfWarriorTokenCount(player1)).isZero();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, elf, true)).isFalse();
    }

    @Test
    @DisplayName("Counts attacking creatures at resolution rather than when cast")
    void countsAttackersAtResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent nonattacker = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        attacker.setAttacking(true);
        harness.castFromHand(player1, new GaladhrimAmbush(), "{3}{G}");
        attacker.setAttacking(false);
        nonattacker.setAttacking(true);
        Permanent additionalAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        additionalAttacker.setAttacking(true);

        harness.passBothPriorities();

        assertThat(elfWarriorTokenCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only Elf attackers deal combat damage to the defending player")
    void onlyElfAttackersDealCombatDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new ElvishWarrior());
        castGaladhrimAmbush();
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Non-Elf combat damage prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGaladhrimAmbush();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, true)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bear, true)).isFalse();
    }

    private void castGaladhrimAmbush() {
        harness.castFromHand(player1, new GaladhrimAmbush(), "{3}{G}");
        harness.passBothPriorities();
    }

    private long elfWarriorTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Elf Warrior".equals(permanent.getCard().getName()))
                .count();
    }
}
