package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupremePhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Other Spirits you control get +1/+1")
    void buffsOtherOwnSpirits() {
        harness.addToBattlefield(player1, new ApothecaryGeist());
        harness.addToBattlefield(player1, new SupremePhantom());

        Permanent geist = findPermanent(player1, "Apothecary Geist");
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(4);
    }

    @Test
    @DisplayName("Supreme Phantom does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new SupremePhantom());

        Permanent phantom = findPermanent(player1, "Supreme Phantom");
        assertThat(gqs.getEffectivePower(gd, phantom)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, phantom)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Spirit creatures")
    void doesNotBuffNonSpirits() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SupremePhantom());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Spirits")
    void doesNotBuffOpponentSpirits() {
        harness.addToBattlefield(player1, new SupremePhantom());
        harness.addToBattlefield(player2, new ApothecaryGeist());

        Permanent geist = findPermanent(player2, "Apothecary Geist");
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Supreme Phantoms buff each other")
    void twoPhantomsBuffEachOther() {
        harness.addToBattlefield(player1, new SupremePhantom());
        harness.addToBattlefield(player1, new SupremePhantom());

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Supreme Phantom"))
                .map(p -> gqs.getEffectivePower(gd, p)))
                .containsExactly(2, 2);
    }

    @Test
    @DisplayName("Buff wears off when Supreme Phantom leaves the battlefield")
    void buffEndsWhenPhantomLeaves() {
        harness.addToBattlefield(player1, new ApothecaryGeist());
        harness.addToBattlefield(player1, new SupremePhantom());

        gd.playerBattlefields.get(player1.getId())
                .remove(findPermanent(player1, "Supreme Phantom"));

        Permanent geist = findPermanent(player1, "Apothecary Geist");
        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }
}
