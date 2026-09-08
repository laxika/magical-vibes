package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DelraichTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast using alternate cost: sacrifice 3 black creatures")
    void castWithAlternateCost() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new NantukoHusk());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");
        UUID husk = harness.getPermanentId(player1, "Nantuko Husk");

        harness.setHand(player1, List.of(new Delraich()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, husk));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Delraich");
        harness.assertInGraveyard(player1, "Child of Night");
        harness.assertInGraveyard(player1, "Black Knight");
        harness.assertInGraveyard(player1, "Nantuko Husk");
    }

    @Test
    @DisplayName("Can be cast normally with mana")
    void castWithManaCost() {
        harness.setHand(player1, List.of(new Delraich()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Delraich");
    }

    @Test
    @DisplayName("Alternate cost fails if fewer than 3 creatures are sacrificed")
    void alternateCostFailsWithFewerCreatures() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");

        harness.setHand(player1, List.of(new Delraich()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice exactly 3");
    }

    @Test
    @DisplayName("Alternate cost fails if a non-black creature is sacrificed")
    void alternateCostFailsWithNonBlackCreature() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Delraich()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, bears)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }
}
