package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProfaneTutor;
import com.github.laxika.magicalvibes.cards.p.PyriteSpellbomb;
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
        PyriteSpellbomb.class})
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
        Permanent saga = addSagaWithLore(2);
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

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ThePartingOfTheWays());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
