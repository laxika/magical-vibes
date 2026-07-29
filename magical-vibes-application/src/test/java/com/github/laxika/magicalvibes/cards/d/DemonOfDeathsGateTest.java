package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemonOfDeathsGateTest extends BaseCardTest {

    

    @Test
    @DisplayName("Can be cast using alternate cost: sacrifice 3 black creatures and pay 6 life")
    void castWithAlternateCost() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new NantukoHusk());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");
        UUID husk = harness.getPermanentId(player1, "Nantuko Husk");

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, husk));
        harness.passBothPriorities();

        // Demon should be on battlefield
        harness.assertOnBattlefield(player1, "Demon of Death's Gate");

        // Three creatures should be gone from battlefield
        harness.assertNotOnBattlefield(player1, "Child of Night");
        harness.assertNotOnBattlefield(player1, "Black Knight");
        harness.assertNotOnBattlefield(player1, "Nantuko Husk");

        // Three creatures should be in graveyard
        harness.assertInGraveyard(player1, "Child of Night");
        harness.assertInGraveyard(player1, "Black Knight");
        harness.assertInGraveyard(player1, "Nantuko Husk");

        // Life should be reduced by 6
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can be cast normally with mana")
    void castWithManaCost() {
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.addMana(player1, ManaColor.BLACK, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demon of Death's Gate");
    }

    @Test
    @DisplayName("Alternate cost fails if fewer than 3 creatures sacrificed")
    void alternateCostFailsWithFewerCreatures() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice exactly 3");
    }

    @Test
    @DisplayName("Alternate cost fails if non-black creature is sacrificed")
    void alternateCostFailsWithNonBlackCreature() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, bears)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Alternate cost fails if not enough life (5 life, cost 6)")
    void alternateCostFailsWithInsufficientLife() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new NantukoHusk());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");
        UUID husk = harness.getPermanentId(player1, "Nantuko Husk");

        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, husk)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Alternate cost can be paid at exactly 6 life, then its controller loses at 0 life")
    void alternateCostSucceedsAtExactLife() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new NantukoHusk());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");
        UUID husk = harness.getPermanentId(player1, "Nantuko Husk");

        harness.setLife(player1, 6);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, husk));

        assertThat(gd.getLife(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        harness.assertNotOnBattlefield(player1, "Demon of Death's Gate");
    }

    @Test
    @DisplayName("Alternate cost does not spend mana")
    void alternateCostDoesNotSpendMana() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player1, new NantukoHusk());

        UUID child = harness.getPermanentId(player1, "Child of Night");
        UUID knight = harness.getPermanentId(player1, "Black Knight");
        UUID husk = harness.getPermanentId(player1, "Nantuko Husk");

        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player1, List.of(new DemonOfDeathsGate()));
        harness.castCreatureWithAlternateCost(player1, 0, List.of(child, knight, husk));

        // Mana should not be spent
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
}
