package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianDigester;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TetsukoUmezawaFugitiveTest extends BaseCardTest {

    // ===== Power or toughness 1 or less: can't be blocked =====

    @Test
    @DisplayName("1/1 creature you control can't be blocked")
    void oneOneCreatureCantBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent wizard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isTrue();
    }

    @Test
    @DisplayName("2/1 creature you control can't be blocked (toughness 1)")
    void twoOneCreatureCantBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new PhyrexianDigester());

        Permanent digester = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Digester"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, digester)).isTrue();
    }

    @Test
    @DisplayName("0/4 creature you control can't be blocked (power 0)")
    void zeroFourCreatureCantBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new WallOfAir());

        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wall of Air"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, wall)).isTrue();
    }

    @Test
    @DisplayName("Tetsuko itself can't be blocked (1/3, power is 1)")
    void tetsukoItselfCantBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());

        Permanent tetsuko = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tetsuko Umezawa, Fugitive"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, tetsuko)).isTrue();
    }

    // ===== Power and toughness both > 1: can be blocked =====

    @Test
    @DisplayName("2/2 creature you control can still be blocked")
    void twoTwoCreatureCanBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    // ===== Does not affect opponent's creatures =====

    @Test
    @DisplayName("Does not affect opponent's 1/1 creature")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player2, new FugitiveWizard());

        Permanent opponentWizard = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, opponentWizard)).isFalse();
    }

    // ===== Effect removed when Tetsuko leaves =====

    @Test
    @DisplayName("Effect removed when Tetsuko leaves the battlefield")
    void effectRemovedWhenTetsukoLeaves() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent wizard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Tetsuko Umezawa, Fugitive"));

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isFalse();
    }
}
