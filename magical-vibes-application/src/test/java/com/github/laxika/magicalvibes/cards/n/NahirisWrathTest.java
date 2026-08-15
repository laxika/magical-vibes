package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NahirisWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the total mana value of discarded cards")
    void dealsTotalDiscardedManaValueDamageToEachTarget() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(
                new NahirisWrath(), new GiantGrowth(), new SerraAngel())));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithDiscards(player1, 0, 2,
                List.of(chandra.getId(), bears.getId()), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Chandra Nalaar");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature nonplaneswalker permanent")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, new ArrayList<>(List.of(new NahirisWrath(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1,
                List.of(land.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures or planeswalkers");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, new ArrayList<>(List.of(new NahirisWrath(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1,
                List.of(player2.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    @DisplayName("X=0 requires no discard and deals no damage")
    void xZeroDoesNothing() {
        harness.setHand(player1, new ArrayList<>(List.of(new NahirisWrath())));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nahiri's Wrath");
    }
}
