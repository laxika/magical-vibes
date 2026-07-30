package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CumberStone;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
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

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isTrue();
    }

    @Test
    @DisplayName("2/1 creature you control can't be blocked (toughness 1)")
    void twoOneCreatureCantBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new PhyrexianDigester());

        Permanent digester = findPermanent(player1, "Phyrexian Digester");

        assertThat(gqs.hasCantBeBlocked(gd, digester)).isTrue();
    }

    @Test
    @DisplayName("0/4 creature you control can't be blocked (power 0)")
    void zeroFourCreatureCantBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new WallOfAir());

        Permanent wall = findPermanent(player1, "Wall of Air");

        assertThat(gqs.hasCantBeBlocked(gd, wall)).isTrue();
    }

    @Test
    @DisplayName("Tetsuko itself can't be blocked (1/3, power is 1)")
    void tetsukoItselfCantBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());

        Permanent tetsuko = findPermanent(player1, "Tetsuko Umezawa, Fugitive");

        assertThat(gqs.hasCantBeBlocked(gd, tetsuko)).isTrue();
    }

    // ===== Power and toughness both > 1: can be blocked =====

    @Test
    @DisplayName("2/2 creature you control can still be blocked")
    void twoTwoCreatureCanBeBlocked() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    // ===== Does not affect opponent's creatures =====

    @Test
    @DisplayName("Does not affect opponent's 1/1 creature")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player2, new FugitiveWizard());

        Permanent opponentWizard = findPermanent(player2, "Fugitive Wizard");

        assertThat(gqs.hasCantBeBlocked(gd, opponentWizard)).isFalse();
    }

    // ===== Layered power and toughness decide the set (CR 613.11) =====

    @Test
    @DisplayName("An anthem lifting a 1/1 to 2/2 takes the evasion away")
    void anthemRemovesEvasion() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isTrue();

        // CR 613.11: the restriction is applied after every other continuous effect, so the
        // matching set reads the layer-7c boost. The 1/1 is a 2/2 and no longer qualifies.
        harness.addToBattlefield(player1, new GloriousAnthem());
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Cumber Stone dropping a 2/2 to 1/2 confers the evasion")
    void opponentDebuffConfersEvasion() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();

        // The same reading in the other direction: -1/-0 from an opponent's permanent makes the
        // 2/2 a 1/2, which qualifies on power.
        harness.addToBattlefield(player2, new CumberStone());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();
    }

    // ===== Effect removed when Tetsuko leaves =====

    @Test
    @DisplayName("Effect removed when Tetsuko leaves the battlefield")
    void effectRemovedWhenTetsukoLeaves() {
        harness.addToBattlefield(player1, new TetsukoUmezawaFugitive());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Tetsuko Umezawa, Fugitive"));

        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isFalse();
    }
}
