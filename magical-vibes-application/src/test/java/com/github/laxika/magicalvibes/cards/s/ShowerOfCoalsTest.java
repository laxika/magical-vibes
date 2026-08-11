package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Shower of Coals")
class ShowerOfCoalsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each of up to three targets below threshold")
    void dealsTwoDamageToEachTargetBelowThreshold() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setLife(player2, 20);
        cast(List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Giant Spider"),
                player2.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 4 damage to each of up to three targets with threshold")
    void dealsFourDamageToEachTargetWithThreshold() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(
                new Cancel(), new Cancel(), new Cancel(), new Cancel(),
                new Cancel(), new Cancel(), new Cancel()));
        harness.setLife(player2, 20);
        cast(List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Giant Spider"),
                player2.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void canBeCastWithNoTargets() {
        harness.setLife(player2, 20);
        cast(List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Shower of Coals");
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new ShowerOfCoals()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
