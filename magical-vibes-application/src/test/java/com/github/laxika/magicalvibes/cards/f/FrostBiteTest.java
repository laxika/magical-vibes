package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrostBiteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage without three snow permanents")
    void dealsTwoDamageWithoutThreeSnowPermanents() {
        harness.addToBattlefield(player2, new HillGiant());
        castFrostBite(harness.getPermanentId(player2, "Hill Giant"));

        assertThat(permanentOf(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 3 damage with three snow permanents")
    void dealsThreeDamageWithThreeSnowPermanents() {
        addThreeSnowPermanents();
        harness.addToBattlefield(player2, new HillGiant());
        castFrostBite(harness.getPermanentId(player2, "Hill Giant"));

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Checks snow permanents when it resolves")
    void checksSnowPermanentsWhenItResolves() {
        addThreeSnowPermanents();
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(permanentOf(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void canTargetPlaneswalker() {
        addThreeSnowPermanents();
        Permanent planeswalker = new Permanent(new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        castFrostBite(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new SnowCoveredForest());
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Snow-Covered Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFrostBite(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addThreeSnowPermanents() {
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.addToBattlefield(player1, new SnowCoveredMountain());
    }

    private Permanent permanentOf(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
