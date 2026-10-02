package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcTrail.class, GrizzlyBears.class, GiantSpider.class, LilianaVess.class, Mountain.class})
class ArcTrailTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Arc Trail with 2 creature targets puts it on the stack")
    void castingWithTwoCreatureTargetsPutsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID id1 = harness.getPermanentId(player2, "Grizzly Bears");
        UUID id2 = harness.getPermanentId(player2, "Giant Spider");

        harness.castSorcery(player1, 0, List.of(id1, id2));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetIds()).containsExactly(id1, id2);
    }

    @Test
    @DisplayName("A planeswalker is a legal target for an any-target slot (CR 115.4)")
    void canTargetPlaneswalker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        GameData gd = harness.getGameData();
        var response = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, gd.playerHands.get(player1.getId()).getFirst(),
                        player1.getId(), null);
        assertThat(response.validPermanentIds()).contains(liliana.getId());

        // 2 damage to the planeswalker, 1 to the creature.
        harness.castSorcery(player1, 0, List.of(liliana.getId(), bearsId));
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("A land is not a legal target for an any-target slot")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(mountain.getId(), bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not a legal target");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, spiderId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast with only 1 target")
    void cannotCastWithOnlyOneTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot cast with duplicate targets")
    void cannotCastWithDuplicateTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Casting with invalid card index -1 throws IllegalArgumentException")
    void castingWithNegativeCardIndexThrowsCleanError() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID id1 = harness.getPermanentId(player2, "Grizzly Bears");
        UUID id2 = harness.getPermanentId(player2, "Giant Spider");

        assertThatThrownBy(() -> harness.castSorcery(player1, -1, List.of(id1, id2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Deals 2 damage to first target and 1 damage to second target")
    void dealsOrderedDamageToCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());   // Target 1: 2 damage (dies, 2 toughness)
        harness.addToBattlefield(player2, new GiantSpider());    // Target 2: 1 damage (survives, 4 toughness)
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        harness.castSorcery(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        // GrizzlyBears took 2 damage (dies: 2 >= 2 toughness)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // GiantSpider took 1 damage (survives: 1 < 4 toughness)
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("1 damage from second target slot does not kill a 2/2")
    void secondTargetOneDamageDoesNotKillTwoToughness() {
        harness.addToBattlefield(player2, new GiantSpider());    // Target 1: 2 damage (survives)
        harness.addToBattlefield(player2, new GrizzlyBears());   // Target 2: 1 damage (survives)
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, List.of(spiderId, bearsId));
        harness.passBothPriorities();

        // Both survive
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to player target and 1 damage to creature target")
    void dealsDamageToPlayerAndCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Target 1 (2 dmg): player2, Target 2 (1 dmg): creature
        harness.castSorcery(player1, 0, List.of(player2.getId(), bearsId));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        // GrizzlyBears took 1 damage (survives: 1 < 2)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals damage to both players")
    void dealsDamageToBothPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Partially resolves when first creature target is removed")
    void partiallyResolvesWhenFirstTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Target 1 (2 dmg): creature, Target 2 (1 dmg): player2
        harness.castSorcery(player1, 0, List.of(bearsId, player2.getId()));

        // Remove the creature before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Giant Spider")));
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Arc Trail goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Giant Spider")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arc Trail");
    }

    @Test
    @DisplayName("First target still takes 2 damage when the second target leaves")
    void partiallyResolvesWhenSecondTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), bears.getId()));
        harness.getGameData().playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Arc Trail");
    }

    @Test
    @DisplayName("Arc Trail does not resolve when both targets leave")
    void doesNotResolveWhenBothTargetsRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId(), spider.getId()));
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Arc Trail");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Arc Trail may target its controller's creature")
    void canDamageOwnCreatureAndOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Surviving creatures retain exactly their assigned damage")
    void marksTwoAndOneDamageOnSurvivingCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }
}
