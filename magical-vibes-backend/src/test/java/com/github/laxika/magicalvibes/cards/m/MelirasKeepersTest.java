package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MelirasKeepersTest extends BaseCardTest {

    // ===== Can't have counters put on it =====

    @Test
    @DisplayName("Melira's Keepers can't have -1/-1 counters put on it by Skinrender ETB")
    void cantHaveMinusOneMinusOneCountersFromSkinrender() {
        harness.addToBattlefield(player1, new MelirasKeepers());
        UUID keepersId = harness.getPermanentId(player1, "Melira's Keepers");

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);

        harness.getGameService().playCard(gd, player2, 0, 0, keepersId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Melira's Keepers should still be on the battlefield with no counters
        Permanent keepers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Melira's Keepers"))
                .findFirst().orElseThrow();
        assertThat(keepers.getMinusOneMinusOneCounters()).isZero();
        assertThat(keepers.getEffectivePower()).isEqualTo(4);
        assertThat(keepers.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Melira's Keepers can't have +1/+1 counters put on it")
    void cantHavePlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new MelirasKeepers());

        Permanent keepers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Melira's Keepers"))
                .findFirst().orElseThrow();

        // Verify the query service correctly reports cant-have-counters
        assertThat(gqs.cantHaveCounters(gd, keepers)).isTrue();
    }

    @Test
    @DisplayName("Normal creature can still receive -1/-1 counters")
    void normalCreatureStillReceivesCounters() {
        harness.addToBattlefield(player1, new MelirasKeepers());

        // Verify other creatures are not protected
        Permanent keepers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Melira's Keepers"))
                .findFirst().orElseThrow();
        assertThat(gqs.cantHaveCounters(gd, keepers)).isTrue();

        // A separate creature without the effect should be able to receive counters
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Permanent bearsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.cantHaveCounters(gd, bearsPerm)).isFalse();
    }
}
