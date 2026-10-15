package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPicker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWeatherseedTreaty.class, Forest.class, Island.class, Plains.class, GoblinPicker.class})
class TheWeatherseedTreatyTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I puts a basic land onto the battlefield tapped")
    void chapterISearchesForTappedBasicLand() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new GoblinPicker(), forest));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        Permanent fetchedForest = findPermanent(player1, "Forest");
        assertThat(fetchedForest).isNotNull();
        assertThat(fetchedForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Chapter II creates a 1/1 green Saproling token")
    void chapterIICreatesSaprolingToken() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling).isNotNull();
        assertThat(saproling.getCard().getPower()).isEqualTo(1);
        assertThat(saproling.getCard().getToughness()).isEqualTo(1);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("Chapter III uses Domain to boost a creature and grant trample until end of turn")
    void chapterIIIBoostsByDomainAndGrantsTrample() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinPicker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Chapter III only targets creatures you control")
    void chapterIIITargetsOwnCreatureOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GoblinPicker());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GoblinPicker());
        addSagaWithLore(2);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opposingCreature.getId());
    }

    @Test
    @DisplayName("Read ahead can start at chapter I and search for a basic land")
    void readAheadStartsAtChapterOne() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new TheWeatherseedTreaty(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "1");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "The Weatherseed Treaty").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("Read ahead can start at chapter II without searching the library")
    void readAheadStartsAtChapterTwo() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new TheWeatherseedTreaty(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "2");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saproling");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(findPermanent(player1, "The Weatherseed Treaty").getCounterCount(CounterType.LORE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Read ahead can start at chapter III and skip both earlier chapters")
    void readAheadStartsAtChapterThree() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinPicker());
        harness.addToBattlefield(player1, new Forest());
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.castFromHand(player1, new TheWeatherseedTreaty(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        harness.assertNotOnBattlefield(player1, "Saproling");
        harness.assertNotOnBattlefield(player1, "The Weatherseed Treaty");
        harness.assertInGraveyard(player1, "The Weatherseed Treaty");
    }

    @Test
    @DisplayName("Chapter I allows failing to find even with a basic land available")
    void chapterICanFailToFind() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Domain counts distinct controlled basic land types at resolution and then stays fixed")
    void chapterIIIEvaluatesDomainAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinPicker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.addToBattlefield(player1, new Plains());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter III grants trample even with no basic land types")
    void chapterIIIGrantsTrampleWithZeroDomain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoblinPicker());
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "The Weatherseed Treaty");
    }

    private void addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheWeatherseedTreaty());
        saga.setCounterCount(CounterType.LORE, loreCounters);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
