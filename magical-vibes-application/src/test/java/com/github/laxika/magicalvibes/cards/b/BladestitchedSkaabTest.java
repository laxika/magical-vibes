package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BladestitchedSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Other Zombies you control get +1/+0")
    void buffsOtherZombiesYouControl() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new BladestitchedSkaab());

        Permanent zombie = findPermanent(player1, "Gravecrawler");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bladestitched Skaab does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new BladestitchedSkaab());

        Permanent skaab = findPermanent(player1, "Bladestitched Skaab");

        assertThat(gqs.getEffectivePower(gd, skaab)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skaab)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Zombie creatures")
    void doesNotBuffNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BladestitchedSkaab());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Zombies")
    void doesNotBuffOpponentZombies() {
        harness.addToBattlefield(player1, new BladestitchedSkaab());
        harness.addToBattlefield(player2, new Gravecrawler());

        Permanent opponentZombie = findPermanent(player2, "Gravecrawler");

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Bladestitched Skaabs buff each other with +1/+0")
    void twoSkaabsBuffEachOther() {
        harness.addToBattlefield(player1, new BladestitchedSkaab());
        harness.addToBattlefield(player1, new BladestitchedSkaab());

        List<Permanent> skaabs = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bladestitched Skaab"))
                .toList();

        assertThat(skaabs).hasSize(2);
        for (Permanent skaab : skaabs) {
            assertThat(gqs.getEffectivePower(gd, skaab)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, skaab)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Bonus is removed when Bladestitched Skaab leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BladestitchedSkaab());
        harness.addToBattlefield(player1, new Gravecrawler());

        Permanent zombie = findPermanent(player1, "Gravecrawler");
        Permanent skaab = findPermanent(player1, "Bladestitched Skaab");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(skaab);

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(1);
    }
}
