package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThunderousVelocipede;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AoTheDawnSky.class, Forest.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class,
        MindStone.class, Shock.class, ThunderousVelocipede.class, Terror.class})
class AoTheDawnSkyTest extends BaseCardTest {

    private static final String BATTLEFIELD_MODE =
            "Look at the top seven cards of your library. Put any number of nonland permanent cards with total mana value 4 or less from among them onto the battlefield. Put the rest on the bottom of your library in a random order.";
    private static final String COUNTER_MODE =
            "Put two +1/+1 counters on each permanent you control that's a creature or Vehicle.";

    @Test
    void deathTriggerPutsSelectedPermanentsOntoBattlefieldWithinTotalManaValue() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card giant = new HillGiant();
        Card forest = new Forest();
        Card shock = new Shock();
        Card secondForest = new Forest();
        Card secondShock = new Shock();
        harness.setLibrary(player1, List.of(bears, elves, giant, forest, shock, secondForest, secondShock));
        harness.addToBattlefield(player1, new AoTheDawnSky());

        destroyAo();
        harness.handleListChoice(player1, BATTLEFIELD_MODE);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), elves.getId(), giant.getId());
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.totalManaValueBound()).isEqualTo(4);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), giant.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(giant, forest, shock, secondForest, secondShock);
    }

    @Test
    void deathTriggerPutsCountersOnControlledCreaturesAndVehiclesOnly() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AoTheDawnSky());

        destroyAo();
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void battlefieldModeAllowsChoosingZeroAndLeavesCardsBelowTopSevenUntouched() {
        Card bears = new GrizzlyBears();
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card fifth = new Forest();
        Card sixth = new Forest();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(bears, first, second, third, fourth, fifth, sixth, untouched));
        harness.addToBattlefield(player1, new AoTheDawnSky());

        destroyAo();
        harness.handleListChoice(player1, BATTLEFIELD_MODE);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(untouched, bears, first, second, third, fourth, fifth, sixth);
    }

    @Test
    void battlefieldModeAcceptsNoncreaturePermanentsAtExactTotalManaValueLimit() {
        Card bears = new GrizzlyBears();
        Card stone = new MindStone();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears, stone, forest));
        harness.addToBattlefield(player1, new AoTheDawnSky());

        destroyAo();
        harness.handleListChoice(player1, BATTLEFIELD_MODE);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), stone.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void battlefieldModeWithNoEligibleCardsKeepsShortLibraryIntact() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card expensivePermanent = new AoTheDawnSky();
        harness.setLibrary(player1, List.of(forest, shock, expensivePermanent));
        harness.addToBattlefield(player1, new AoTheDawnSky());

        destroyAo();
        harness.handleListChoice(player1, BATTLEFIELD_MODE);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ao, the Dawn Sky");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, shock, expensivePermanent);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    private void destroyAo() {
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Ao, the Dawn Sky"));
        harness.passBothPriorities();
    }
}
