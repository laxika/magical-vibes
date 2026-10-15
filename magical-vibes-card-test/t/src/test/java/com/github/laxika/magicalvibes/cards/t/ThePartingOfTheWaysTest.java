package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProfaneTutor;
import com.github.laxika.magicalvibes.cards.p.PyriteSpellbomb;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePartingOfTheWays.class, Forest.class, GrizzlyBears.class, ProfaneTutor.class,
        PyriteSpellbomb.class, SolRing.class})
class ThePartingOfTheWaysTest extends BaseCardTest {

    @Test
    void chapterOneExilesFiveCardsAndSuspendsEachNonlandWithItsManaValue() {
        Card suspended = new ProfaneTutor();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card anotherCreature = new GrizzlyBears();
        Card anotherLand = new Forest();
        Permanent saga = addSagaWithLore(0);
        harness.setLibrary(player1, List.of(suspended, creature, land, anotherCreature, anotherLand));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(suspended, creature, land, anotherCreature, anotherLand);
        assertThat(gd.exiledCardTimeCounters)
                .containsEntry(suspended.getId(), suspended.getManaValue())
                .containsEntry(creature.getId(), creature.getManaValue())
                .containsEntry(anotherCreature.getId(), anotherCreature.getManaValue())
                .doesNotContainKeys(land.getId(), anotherLand.getId());
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterTwoTimeTravelsTwice() {
        Permanent saga = addSagaWithLore(1);
        Card suspended = new GrizzlyBears();
        gd.addToExile(player1.getId(), suspended);
        gd.exiledCardTimeCounters.put(suspended.getId(), 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ADD");
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.exiledCardTimeCounters).containsEntry(suspended.getId(), 3);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    void chapterThreeDestroysUpToOneArtifactControlledByTheOpponent() {
        addSagaWithLore(2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PyriteSpellbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId(), player1.getId())
                .doesNotContain(creature.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void chapterOneLeavesTheSixthCardInTheLibrary() {
        addSagaWithLore(0);
        List<Card> topFive = List.of(new Forest(), new SolRing(), new Forest(), new SolRing(), new Forest());
        Card sixth = new SolRing();
        harness.setLibrary(player1, List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), sixth));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(topFive);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth);
    }

    @Test
    void chapterOneExilesAllCardsFromAShortLibrary() {
        addSagaWithLore(0);
        Card land = new Forest();
        Card artifact = new SolRing();
        harness.setLibrary(player1, List.of(land, artifact));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, artifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).containsEntry(artifact.getId(), 1)
                .doesNotContainKey(land.getId());
    }

    @Test
    void zeroManaCardCannotBeTimeTravelledOrCastFromSuspend() {
        addSagaWithLore(0);
        Card zeroMana = new ProfaneTutor();
        harness.setLibrary(player1, List.of(zeroMana));
        advanceToNextChapter();
        harness.passBothPriorities();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(zeroMana);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chapterTwoCanCastACardThatGainedSuspendFromChapterOne() {
        addSagaWithLore(0);
        Card artifact = new SolRing();
        harness.setLibrary(player1, List.of(artifact));
        advanceToNextChapter();
        harness.passBothPriorities();

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(artifact.getId()));
    }

    @Test
    void chapterTwoCanSkipBothTravelsOnAControlledPermanent() {
        addSagaWithLore(1);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        ownArtifact.setCounterCount(CounterType.TIME, 1);
        opposingArtifact.setCounterCount(CounterType.TIME, 1);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SKIP");
        harness.handleListChoice(player1, "SKIP");

        assertThat(ownArtifact.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(opposingArtifact.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterThreeCanChooseNoArtifactAndStillSacrificesTheSaga() {
        Permanent saga = addSagaWithLore(2);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opposingArtifact.getId(), player1.getId())
                .doesNotContain(ownArtifact.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact).doesNotContain(saga);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterThreeDestroysOnlyOneArtifactFromTheSameOpponent() {
        addSagaWithLore(2);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SolRing());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ThePartingOfTheWays());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
