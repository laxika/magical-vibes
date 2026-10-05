package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfDiscovery.class, Forest.class, GrizzlyBears.class, MomentOfCraving.class,
        RayOfCommand.class, Opalescence.class})
class PathOfDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering under your control explores and puts a revealed land into your hand")
    void enteringCreatureExploresLand() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        Permanent creature = castEnteringCreature();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(land.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature entering under your control explores and gets a counter for a revealed nonland")
    void enteringCreatureExploresNonland() {
        Permanent path = harness.addToBattlefieldAndReturn(player1, new PathOfDiscovery());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));

        Permanent creature = castEnteringCreature();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(path.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonland.getId());
    }

    @Test
    @DisplayName("The revealed nonland can be put into the graveyard")
    void enteringCreatureExploresAndMillsNonland() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        Card nonland = new GrizzlyBears();
        Card nextCard = new Forest();
        harness.setLibrary(player1, List.of(nonland, nextCard));

        Permanent creature = castEnteringCreature();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Exploring an empty library still adds a counter")
    void enteringCreatureExploresEmptyLibrary() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        harness.setLibrary(player1, List.of());

        Permanent creature = castEnteringCreature();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's creature does not explore")
    void opponentsCreatureDoesNotExplore() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent creature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature permanent entering does not explore")
    void noncreatureDoesNotExplore() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.enterBattlefieldAndReturn(player1, new PathOfDiscovery());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The creature still explores after it leaves the battlefield")
    void removedCreatureStillExplores() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        Permanent creature = castEnteringCreature();

        harness.setHand(player2, List.of(new MomentOfCraving()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature that changes control explores its new controller's library")
    void stolenCreatureExploresNewControllersLibrary() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        Card originalControllersLand = new Forest();
        Card newControllersLand = new Forest();
        harness.setLibrary(player1, List.of(originalControllersLand));
        harness.setLibrary(player2, List.of(newControllersLand));
        Permanent creature = castEnteringCreature();

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(newControllersLand);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalControllersLand);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(originalControllersLand);
    }

    @Test
    @DisplayName("Each Path of Discovery causes a separate explore")
    void multiplePathsExploreSeparately() {
        harness.addToBattlefield(player1, new PathOfDiscovery());
        harness.addToBattlefield(player1, new PathOfDiscovery());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        Permanent creature = castEnteringCreature();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Path of Discovery explores itself when it enters as a creature")
    @CardUsed({PathOfDiscovery.class, Forest.class, Opalescence.class})
    void animatedPathExploresItselfOnEntry() {
        harness.addToBattlefield(player1, new Opalescence());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PathOfDiscovery(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent castEnteringCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Grizzly Bears");
    }
}
