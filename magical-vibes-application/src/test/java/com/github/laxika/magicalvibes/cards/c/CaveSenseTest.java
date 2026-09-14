package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DartingMerfolk;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaveSense.class, CreditVoucher.class, DartingMerfolk.class, Mountain.class})
class CaveSenseTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsBoostAndMountainwalk() {
        Permanent bears = addCreatureReady(player1, new DartingMerfolk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CaveSense());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    void canEnchantOpponentCreature() {
        Permanent merfolk = addCreatureReady(player2, new DartingMerfolk());
        int basePower = gqs.getEffectivePower(gd, merfolk);
        int baseToughness = gqs.getEffectiveToughness(gd, merfolk);
        harness.setHand(player1, List.of(new CaveSense()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, merfolk.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Cave Sense");
        assertThat(aura.getAttachedTo()).isEqualTo(merfolk.getId());
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    void mountainwalkPreventsBlockingWhileDefendingPlayerControlsMountain() {
        Permanent attacker = addCreatureReady(player1, new DartingMerfolk());
        attacker.setAttacking(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CaveSense());
        aura.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new DartingMerfolk());

        assertThat(bls.getBlockingIllegalityReason(
                gd, blocker, attacker, gd.playerBattlefields.get(player2.getId()))).isEmpty();

        harness.addToBattlefield(player2, new Mountain());

        assertThat(bls.getBlockingIllegalityReason(
                gd, blocker, attacker, gd.playerBattlefields.get(player2.getId())))
                .hasValue("Darting Merfolk can't be blocked (mountainwalk)");
    }

    @Test
    void effectsStopWhenAuraLeavesBattlefield() {
        Permanent bears = addCreatureReady(player1, new DartingMerfolk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CaveSense());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    void fizzlesIfTargetCreatureIsRemovedBeforeResolution() {
        Permanent bears = addCreatureReady(player1, new DartingMerfolk());
        harness.setHand(player1, List.of(new CaveSense()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cave Sense");
        harness.assertNotOnBattlefield(player1, "Cave Sense");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new CreditVoucher());
        harness.setHand(player1, List.of(new CaveSense()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Credit Voucher");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
