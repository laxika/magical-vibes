package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MengHuoBarbarianKingTest extends BaseCardTest {

    // ===== Static effect: buffs other green creatures you control =====

    @Test
    @DisplayName("Other green creatures you control get +1/+1")
    void buffsOtherGreenCreatures() {
        harness.addToBattlefield(player1, new MengHuoBarbarianKing());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        // 2/2 base + 1/1 from Meng Huo = 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Meng Huo does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MengHuoBarbarianKing());

        Permanent mengHuo = findPermanent(player1, "Meng Huo, Barbarian King");
        // 4/4 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, mengHuo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mengHuo)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff non-green creatures")
    void doesNotBuffNonGreenCreatures() {
        harness.addToBattlefield(player1, new MengHuoBarbarianKing());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent giant = findPermanent(player1, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's green creatures")
    void doesNotBuffOpponentGreenCreatures() {
        harness.addToBattlefield(player1, new MengHuoBarbarianKing());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        // 2/2 base, no buff from opponent's Meng Huo
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    // ===== Bonus removed when Meng Huo leaves =====

    @Test
    @DisplayName("Bonus is removed when Meng Huo leaves the battlefield")
    void bonusRemovedWhenMengHuoLeaves() {
        harness.addToBattlefield(player1, new MengHuoBarbarianKing());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Meng Huo, Barbarian King"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
