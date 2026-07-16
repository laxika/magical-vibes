package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to each creature without flying, killing small ones")
    void etbKillsNonFlyers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castThunderDragon();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB damages larger non-flyers without killing them")
    void etbDamagesLargeNonFlyers() {
        harness.addToBattlefield(player2, new GiantSpider());
        castThunderDragon();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent spider = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Giant Spider"))
                .findFirst().orElseThrow();
        assertThat(spider.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB does not damage creatures with flying")
    void etbSparesFlyers() {
        harness.addToBattlefield(player2, new AirElemental());
        castThunderDragon();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent flyer = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        assertThat(flyer.getMarkedDamage()).isEqualTo(0);
    }

    private void castThunderDragon() {
        harness.setHand(player1, List.of(new ThunderDragon()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0);
    }
}
