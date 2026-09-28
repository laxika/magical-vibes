package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    private void castGaladhrimAmbush() {
        harness.setHand(player1, List.of(new GaladhrimAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private long elfWarriorTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Elf Warrior".equals(permanent.getCard().getName()))
                .count();
    }
}
