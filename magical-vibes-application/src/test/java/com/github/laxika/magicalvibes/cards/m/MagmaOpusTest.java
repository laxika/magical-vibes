package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagmaOpusTest extends BaseCardTest {

    @Test
    void dealsDividedDamageTapsPermanentsCreatesElementalAndDrawsTwoCards() {
        Permanent damageTarget1 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent damageTarget2 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent tapTarget1 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent tapTarget2 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears drawn1 = new GrizzlyBears();
        GrizzlyBears drawn2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn1, drawn2));
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 0, null,
                Map.of(damageTarget1.getId(), 2, damageTarget2.getId(), 2),
                List.of(tapTarget1.getId(), tapTarget2.getId()), List.of());
        harness.passBothPriorities();

        assertThat(damageTarget1.getMarkedDamage()).isEqualTo(2);
        assertThat(damageTarget2.getMarkedDamage()).isEqualTo(2);
        assertThat(tapTarget1.isTapped()).isTrue();
        assertThat(tapTarget2.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Elemental")).singleElement().satisfies(elemental -> {
            assertThat(elemental.getEffectivePower()).isEqualTo(4);
            assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
        });
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn1, drawn2);
    }

    @Test
    void requiresTwoTapTargets() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null,
                Map.of(damageTarget.getId(), 4), List.of(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void handAbilityCreatesTreasureAndDiscardsMagmaOpus() {
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
        harness.assertInGraveyard(player1, "Magma Opus");
    }
}
