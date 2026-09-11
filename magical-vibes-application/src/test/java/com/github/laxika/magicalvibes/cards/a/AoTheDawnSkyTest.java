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
        Permanent vehicle = new Permanent(new ThunderousVelocipede());
        gd.playerBattlefields.get(player1.getId()).add(vehicle);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AoTheDawnSky());

        destroyAo();
        harness.handleListChoice(player1, COUNTER_MODE);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyAo() {
        Permanent ao = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AoTheDawnSky)
                .findFirst()
                .orElseThrow();
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, ao.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
