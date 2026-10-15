package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CenoteScout;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycoidMaze;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwistsAndTurns.class, MycoidMaze.class, CenoteScout.class, Forest.class,
        GrizzlyBears.class, Shock.class})
class TwistsAndTurnsTest extends BaseCardTest {

    @Test
    void entersAndMakesTargetCreatureScryBeforeExploring() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card nonland = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(nonland, land));
        harness.setHand(player1, List.of(new TwistsAndTurns()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleCopiesAddOneScryBeforeEachExplore() {
        harness.addToBattlefield(player1, new TwistsAndTurns());
        harness.addToBattlefield(player1, new TwistsAndTurns());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new CenoteScout()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    void transformsWhenTheSeventhLandEnters() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new TwistsAndTurns());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isTrue();
    }

    @Test
    void backFaceManaAbilityAddsGreen() {
        Permanent maze = addTransformedMaze(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(maze.isTapped()).isTrue();
    }

    @Test
    void backFaceSearchesTopFourForCreature() {
        addTransformedMaze(player1);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Shock(), creature, new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(creature);
    }

    private Permanent addTransformedMaze(Player player) {
        TwistsAndTurns frontFace = new TwistsAndTurns();
        Permanent maze = harness.addToBattlefieldAndReturn(player, frontFace);
        maze.setSummoningSick(false);
        maze.setCard(frontFace.getBackFaceCard());
        maze.setTransformed(true);
        return maze;
    }

    @Test
    void exploreCanKeepANonlandAfterScrying() {
        harness.addToBattlefield(player1, new TwistsAndTurns());
        Card nonland = new CenoteScout();
        harness.setLibrary(player1, List.of(nonland));
        harness.setHand(player1, List.of(new CenoteScout()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        Permanent scout = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof CenoteScout).findFirst().orElseThrow();
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    void exploreCanPutANonlandIntoTheGraveyardAfterScrying() {
        harness.addToBattlefield(player1, new TwistsAndTurns());
        Card nonland = new CenoteScout();
        harness.setLibrary(player1, List.of(nonland));
        harness.setHand(player1, List.of(new CenoteScout()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, true);

        Permanent scout = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof CenoteScout).findFirst().orElseThrow();
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    void emptyLibraryStillGivesTheExploringCreatureACounter() {
        harness.addToBattlefield(player1, new TwistsAndTurns());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CenoteScout()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent scout = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof CenoteScout).findFirst().orElseThrow();
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotScryForAnOpponentsExploringCreature() {
        harness.addToBattlefield(player1, new TwistsAndTurns());
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));
        harness.setHand(player2, List.of(new CenoteScout()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
    }

    @Test
    void sixthLandDoesNotTriggerTransformation() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new TwistsAndTurns());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(enchantment.isTransformed()).isFalse();
    }

    @Test
    void entersTriggerCanOnlyTargetYourOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CenoteScout());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CenoteScout());
        harness.setHand(player1, List.of(new TwistsAndTurns()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId())
                .doesNotContain(opposingCreature.getId());
    }

    @Test
    void transformedMazeNoLongerAddsScryToExplore() {
        addTransformedMaze(player1);
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new CenoteScout()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
    }

    @Test
    void transformationRechecksLandCountWhenItResolves() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new TwistsAndTurns());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isFalse();
    }

    @Test
    void backFaceCanDeclineTheCreatureAndKeepsUnlookedCardsOnTop() {
        Permanent maze = addTransformedMaze(player1);
        Card creature = new CenoteScout();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card thirdLand = new Forest();
        Card unlookedCard = new CenoteScout();
        harness.setLibrary(player1, List.of(creature, firstLand, secondLand, thirdLand, unlookedCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(maze.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unlookedCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(creature, firstLand, secondLand, thirdLand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void backFaceWorksWithFewerThanFourCards() {
        addTransformedMaze(player1);
        Card creature = new CenoteScout();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void backFaceWithNoCreatureLeavesAllLookedCardsInTheLibrary() {
        addTransformedMaze(player1);
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
