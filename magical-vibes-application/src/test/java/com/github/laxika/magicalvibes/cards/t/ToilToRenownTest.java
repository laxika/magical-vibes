package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.cards.w.WallOfTanglecord;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToilToRenownTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each tapped artifact, creature, and land you control")
    void gainsLifeForEachTappedPermanent() {
        // 1 tapped creature + 1 tapped artifact + 1 tapped land = 3 life
        add(player1, new GrizzlyBears(), true);
        add(player1, new MoxOpal(), true);
        add(player1, new Forest(), true);

        harness.setLife(player1, 20);
        cast(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Untapped permanents are not counted")
    void untappedPermanentsNotCounted() {
        add(player1, new GrizzlyBears(), true);
        add(player1, new MoxOpal(), false);
        add(player1, new Forest(), false);

        harness.setLife(player1, 20);
        cast(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("An artifact creature is counted only once")
    void artifactCreatureCountedOnce() {
        add(player1, new WallOfTanglecord(), true);

        harness.setLife(player1, 20);
        cast(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Only permanents you control are counted")
    void onlyControllerPermanentsCounted() {
        add(player1, new GrizzlyBears(), true);
        add(player2, new GrizzlyBears(), true);
        add(player2, new Forest(), true);

        harness.setLife(player1, 20);
        cast(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Gains no life when no tapped permanents are controlled")
    void gainsNoLifeWhenNoneTapped() {
        add(player1, new GrizzlyBears(), false);

        harness.setLife(player1, 20);
        cast(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new ToilToRenown()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castInstant(player, 0);
        harness.passBothPriorities();
    }

    private Permanent add(Player player, Card card, boolean tapped) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
