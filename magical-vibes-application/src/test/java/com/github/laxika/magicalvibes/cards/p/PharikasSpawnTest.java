package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HavengulLich;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PharikasSpawn.class, NyxbornCourser.class, HavengulLich.class})
class PharikasSpawnTest extends BaseCardTest {

    @Test
    void normalCastDoesNotGetEscapeBonusOrMakeOpponentsSacrifice() {
        harness.setHand(player1, List.of(new PharikasSpawn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addToBattlefield(player2, new NyxbornCourser());
        harness.addToBattlefield(player2, new PharikasSpawn());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent spawn = findPermanent(player1, "Pharika's Spawn");
        assertThat(spawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Nyxborn Courser");
        harness.assertOnBattlefield(player2, "Pharika's Spawn");
    }

    @Test
    void escapeExilesThreeCardsAddsCountersAndMakesEachOpponentSacrificeNonGorgonCreature() {
        PharikasSpawn spawn = new PharikasSpawn();
        NyxbornCourser first = new NyxbornCourser();
        NyxbornCourser second = new NyxbornCourser();
        NyxbornCourser third = new NyxbornCourser();
        harness.setGraveyard(player1, List.of(spawn, first, second, third));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addToBattlefield(player2, new NyxbornCourser());
        harness.addToBattlefield(player2, new PharikasSpawn());

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);

        harness.passBothPriorities();
        Permanent escapedSpawn = findPermanent(player1, "Pharika's Spawn");
        assertThat(escapedSpawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Nyxborn Courser");
        harness.assertOnBattlefield(player2, "Pharika's Spawn");
        harness.assertOnBattlefield(player1, "Pharika's Spawn");
    }

    @Test
    void escapeRequiresThreeOtherCardsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(new PharikasSpawn(), new NyxbornCourser(), new NyxbornCourser()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingWithLichPermissionDoesNotGetEscapeCounters() {
        PharikasSpawn spawn = new PharikasSpawn();
        harness.addToBattlefield(player1, new HavengulLich());
        harness.setGraveyard(player1, List.of(spawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, spawn.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromGraveyard(player1, spawn.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Pharika's Spawn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castingWithLichPermissionDoesNotMakeOpponentSacrifice() {
        PharikasSpawn spawn = new PharikasSpawn();
        harness.addToBattlefield(player1, new HavengulLich());
        harness.addToBattlefield(player2, new NyxbornCourser());
        harness.setGraveyard(player1, List.of(spawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, spawn.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromGraveyard(player1, spawn.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Nyxborn Courser");
    }

    @Test
    void opponentChoosesExactlyOneNonGorgonAndControllerKeepsTheirCreatures() {
        NyxbornCourser chosen = new NyxbornCourser();
        NyxbornCourser retained = new NyxbornCourser();
        harness.setGraveyard(player1, List.of(new PharikasSpawn(),
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser()));
        harness.addToBattlefield(player1, new NyxbornCourser());
        Permanent chosenPermanent = harness.addToBattlefieldAndReturn(player2, chosen);
        harness.addToBattlefield(player2, retained);
        harness.addToBattlefield(player2, new PharikasSpawn());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2,
                List.of(chosenPermanent.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chosen).doesNotContain(retained);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard).contains(retained);
        harness.assertOnBattlefield(player2, "Pharika's Spawn");
        harness.assertOnBattlefield(player1, "Nyxborn Courser");
    }

    @Test
    void escapeWithOnlyOpposingGorgonsStillGetsCountersWithoutSacrifice() {
        harness.setGraveyard(player1, List.of(new PharikasSpawn(),
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser()));
        harness.addToBattlefield(player2, new PharikasSpawn());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Pharika's Spawn")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Pharika's Spawn");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
