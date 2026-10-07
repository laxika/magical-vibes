package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonTitan.class, Forest.class, Plains.class, GrizzlyBears.class})
class SummonTitanTest extends BaseCardTest {

    @Test
    void chapterIMillsFiveCards() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new Plains(), new GrizzlyBears(), new Forest(),
                new Plains()));
        addSagaWithLore(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getType)
                .containsExactlyInAnyOrder(CardType.CREATURE, CardType.LAND, CardType.LAND,
                        CardType.CREATURE, CardType.LAND);
    }

    @Test
    void chapterIIReturnsAllLandsFromGraveyardTapped() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, bear, plains));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .extracting(Permanent::isTapped)
                .containsExactly(true, true);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(bear.getId()))
                .isEmpty();
    }

    @Test
    void chapterIIIBoostsAnotherControlledCreatureByLandCountAndGrantsTrample() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent saga = addSagaWithLore(2);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        assertThat(choice.validIds()).doesNotContain(saga.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void enteringBattlefieldTriggersChapterI() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Plains(), new Forest(), new Plains(), new Forest(), new Plains()));

        harness.enterBattlefieldAndReturn(player1, new SummonTitan());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    void chapterIMillsOnlyAvailableCardsWithoutMillingOpponent() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        Forest opponentCard = new Forest();
        harness.setLibrary(player1, List.of(forest, plains));
        harness.setLibrary(player2, List.of(opponentCard));
        addSagaWithLore(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest, plains);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void chapterIIReturnsOnlyControllersLandsAndLeavesNonlandsInGraveyard() {
        Forest land = new Forest();
        SummonTitan nonland = new SummonTitan();
        Plains opponentLand = new Plains();
        harness.setGraveyard(player1, List.of(land, nonland));
        harness.setGraveyard(player2, List.of(opponentLand));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(land.getId()))
                .extracting(Permanent::isTapped).containsExactly(true);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player2, "Plains");
    }

    @Test
    void chapterIIResolvesWithoutLandCardsInGraveyard() {
        SummonTitan nonland = new SummonTitan();
        harness.setGraveyard(player1, List.of(nonland));
        Permanent saga = addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(saga);
    }

    @Test
    void chapterIIICountsLandsAtResolutionAndLocksInTheBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());
        addSagaWithLore(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Summon: Titan");
        harness.assertInGraveyard(player1, "Summon: Titan");

        harness.addToBattlefield(player1, new Forest());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void chapterIIIGrantsTrampleEvenWithNoLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSagaWithLore(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void chapterIIIMustChooseALegalCreatureWhenOneIsAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SummonTitan());
        addSagaWithLore(2);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
    }

    @Test
    void chapterIIIWithoutLegalTargetsSacrificesSagaBeforePlayersReceivePriority() {
        addSagaWithLore(2);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SummonTitan());

        triggerChapter();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Summon: Titan");
        harness.assertInGraveyard(player1, "Summon: Titan");
        harness.assertOnBattlefield(player2, "Summon: Titan");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonTitan());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
    }
}
