package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DarkSalvation;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NahirisWrath.class, ChandraNalaar.class, DarkSalvation.class, GiantGrowth.class, GrizzlyBears.class,
        Mountain.class, SerraAngel.class})
class NahirisWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the total mana value of discarded cards")
    void dealsTotalDiscardedManaValueDamageToEachTarget() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NahirisWrath(), new GiantGrowth(), new SerraAngel()));
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
        harness.setHand(player1, List.of(new NahirisWrath(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1,
                List.of(land.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures or planeswalkers");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new NahirisWrath(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1,
                List.of(player2.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    @DisplayName("X=0 requires no discard and deals no damage")
    void xZeroDoesNothing() {
        harness.setHand(player1, List.of(new NahirisWrath()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nahiri's Wrath");
    }

    @Test
    void canChooseFewerTargetsThanDiscardedCards() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player1, List.of(new GiantGrowth(), new NahirisWrath(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithDiscards(player1, 1, 2, List.of(angel.getId()), List.of(0, 2));

        harness.assertInGraveyard(player1, "Giant Growth");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(angel.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void canDiscardCardsWithoutChoosingAnyTargets() {
        harness.setHand(player1, List.of(new NahirisWrath(), new SerraAngel()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(), List.of(1));

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Nahiri's Wrath");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void discardingALandDealsZeroDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NahirisWrath(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(bears.getId()), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    void xInADiscardedCardsManaCostCountsAsZero() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NahirisWrath(), new DarkSalvation()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(bears.getId()), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark Salvation");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotChooseMoreTargetsThanDiscardedCardsEvenWhenManaValueIsHigher() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new NahirisWrath(), new SerraAngel()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1,
                List.of(bears.getId(), angel.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameTargetTwice() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new NahirisWrath(), new GiantGrowth(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 2,
                List.of(angel.getId(), angel.getId()), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mustDiscardExactlyTheAnnouncedNumberOfCards() {
        harness.setHand(player1, List.of(new NahirisWrath(), new GiantGrowth(), new SerraAngel()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 2,
                List.of(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotDiscardTheSpellToPayItsOwnCost() {
        harness.setHand(player1, List.of(new NahirisWrath()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1,
                List.of(), List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
