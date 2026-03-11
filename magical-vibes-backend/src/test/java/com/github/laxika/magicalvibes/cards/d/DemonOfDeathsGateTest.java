package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.AlternateCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DemonOfDeathsGateTest extends BaseCardTest {

    @Test
    @DisplayName("Has alternate casting cost configured")
    void hasAlternateCastingCost() {
        DemonOfDeathsGate card = new DemonOfDeathsGate();

        AlternateCastingCost altCost = card.getAlternateCastingCost();
        assertThat(altCost).isNotNull();
        assertThat(altCost.lifeCost()).isEqualTo(6);
        assertThat(altCost.sacrificeCount()).isEqualTo(3);
        assertThat(altCost.sacrificeFilter()).isInstanceOf(PermanentAllOfPredicate.class);
    }

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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Demon of Death's Gate"));

        // Three creatures should be gone from battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Child of Night"))
                .noneMatch(p -> p.getCard().getName().equals("Black Knight"))
                .noneMatch(p -> p.getCard().getName().equals("Nantuko Husk"));

        // Three creatures should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Child of Night"))
                .anyMatch(c -> c.getName().equals("Black Knight"))
                .anyMatch(c -> c.getName().equals("Nantuko Husk"));

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Demon of Death's Gate"));
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
    @DisplayName("Alternate cost succeeds at exactly 6 life (leaving 0 per CR 119.4)")
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
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Demon of Death's Gate"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(0);
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
