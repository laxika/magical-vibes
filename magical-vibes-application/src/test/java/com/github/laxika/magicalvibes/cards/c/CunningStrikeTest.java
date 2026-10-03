package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UginTheSpiritDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningStrike.class, GrizzlyBears.class, UginTheSpiritDragon.class})
class CunningStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a creature and a player, then draws a card")
    void damagesCreatureAndPlayerAndDrawsCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CunningStrike()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                player2.getId()));

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires the second target to be a player or planeswalker")
    void rejectsCreatureAsPlayerOrPlaneswalkerTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new CunningStrike()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creatureId, creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesPlaneswalkerAndCreatureAndDrawsCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon())
                .setCounterCount(CounterType.LOYALTY, 7);
        UUID uginId = harness.getPermanentId(player2, "Ugin, the Spirit Dragon");
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"), uginId));

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(uginId)).findFirst().orElseThrow()
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void stillDamagesPlayerAndDrawsWhenCreatureTargetLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        prepareSpell();
        harness.castInstant(player1, 0, List.of(creatureId, player2.getId()));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cunning Strike");
    }

    @Test
    void stillDamagesCreatureAndDrawsWhenPlaneswalkerTargetLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon())
                .setCounterCount(CounterType.LOYALTY, 7);
        UUID uginId = harness.getPermanentId(player2, "Ugin, the Spirit Dragon");
        prepareSpell();
        harness.castInstant(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"), uginId));
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(uginId));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void doesNotDrawWhenBothTargetsLeave() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon())
                .setCounterCount(CounterType.LOYALTY, 7);
        prepareSpell();
        harness.castInstant(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Ugin, the Spirit Dragon")));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Cunning Strike");
        harness.assertLife(player2, 20);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new CunningStrike()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
