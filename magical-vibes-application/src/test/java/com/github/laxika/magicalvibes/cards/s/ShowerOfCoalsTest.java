package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EmberBeast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StoneTongueBasilisk;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Shower of Coals")
@CardUsed({ShowerOfCoals.class, WildMongrel.class, EmberBeast.class,
        StoneTongueBasilisk.class, Forest.class})
class ShowerOfCoalsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each of up to three targets below threshold")
    void dealsTwoDamageToEachTargetBelowThreshold() {
        harness.addToBattlefield(player2, new WildMongrel());
        harness.addToBattlefield(player2, new EmberBeast());
        harness.setLife(player2, 20);
        cast(List.of(
                harness.getPermanentId(player2, "Wild Mongrel"),
                harness.getPermanentId(player2, "Ember Beast"),
                player2.getId()));

        harness.assertNotOnBattlefield(player2, "Wild Mongrel");
        harness.assertOnBattlefield(player2, "Ember Beast");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 4 damage to each of up to three targets with threshold")
    void dealsFourDamageToEachTargetWithThreshold() {
        harness.addToBattlefield(player2, new WildMongrel());
        harness.addToBattlefield(player2, new EmberBeast());
        harness.addToBattlefield(player2, new StoneTongueBasilisk());
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLife(player2, 20);
        cast(List.of(
                harness.getPermanentId(player2, "Wild Mongrel"),
                harness.getPermanentId(player2, "Ember Beast"),
                player2.getId()));

        harness.assertNotOnBattlefield(player2, "Wild Mongrel");
        harness.assertNotOnBattlefield(player2, "Ember Beast");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player2, "Stone-Tongue Basilisk");
    }

    @Test
    @DisplayName("Uses normal damage with six cards in your graveyard even if an opponent has seven")
    void usesNormalDamageWithSixCardsInYourGraveyard() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLife(player2, 20);
        cast(List.of(player2.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
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
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }
}
