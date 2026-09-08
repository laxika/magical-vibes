package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KitsuneBonesetterTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 3 damage to a target creature when ahead in hand size")
    void preventsNextThreeDamageWhenControllerHasMoreCards() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter),
                null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot activate when hand sizes are tied")
    void cannotActivateWhenHandSizesAreTied() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent bonesetter = addCreatureReady(player1, new KitsuneBonesetter());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(bonesetter), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
