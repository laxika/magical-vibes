package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SquallTest extends BaseCardTest {

    private void castSquall() {
        harness.setHand(player1, List.of(new Squall()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Squall destroys a flying creature with 2 or less toughness")
    void destroysSmallFlyer() {
        harness.addToBattlefield(player2, new SuntailHawk());

        castSquall();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Squall marks 2 damage on a larger flyer that survives")
    void damagesLargeFlyer() {
        harness.addToBattlefield(player2, new AirElemental());

        castSquall();

        GameData gd = harness.getGameData();
        Permanent flyer = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Air Elemental"))
                .findFirst().orElseThrow();
        assertThat(flyer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Squall does not damage non-flying creatures, including reach")
    void ignoresNonFlyers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castSquall();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears")
                        || p.getCard().getName().equals("Giant Spider"))
                .allSatisfy(p -> assertThat(p.getMarkedDamage()).isEqualTo(0));
    }

    @Test
    @DisplayName("Squall does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSquall();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
