package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YargleGluttonOfUrborg;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheNightOfTheDoctor.class, GrizzlyBears.class, YargleGluttonOfUrborg.class})
class TheNightOfTheDoctorTest extends BaseCardTest {

    @Test
    void chapterIDestroysAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheNightOfTheDoctor());
        saga.setCounterCount(CounterType.LORE, 0);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    void chapterIIReturnsLegendaryCreatureWithFirstStrikeCounter() {
        assertChapterIICounter("Put a first strike counter on it", CounterType.FIRST_STRIKE, Keyword.FIRST_STRIKE);
    }

    @Test
    void chapterIIReturnsLegendaryCreatureWithVigilanceCounter() {
        assertChapterIICounter("Put a vigilance counter on it", CounterType.VIGILANCE, Keyword.VIGILANCE);
    }

    @Test
    void chapterIIReturnsLegendaryCreatureWithLifelinkCounter() {
        assertChapterIICounter("Put a lifelink counter on it", CounterType.LIFELINK, Keyword.LIFELINK);
    }

    @Test
    void enteringTriggersChapterIAndLeavesNoncreaturesAlone() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingSaga = harness.addToBattlefieldAndReturn(player2, new TheNightOfTheDoctor());

        Permanent saga = harness.enterBattlefieldAndReturn(player1, new TheNightOfTheDoctor());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingSaga).doesNotContain(opposingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature.getCard());
    }

    @Test
    void chapterIIWithNoLegalTargetSacrificesSagaWithoutOfferingCounterChoice() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheNightOfTheDoctor());
        saga.setCounterCount(CounterType.LORE, 1);
        Card nonLegendaryCreature = new GrizzlyBears();
        Card nonCreature = new TheNightOfTheDoctor();
        Card opposingLegendaryCreature = new YargleGluttonOfUrborg();
        harness.setGraveyard(player1, List.of(nonLegendaryCreature, nonCreature));
        harness.setGraveyard(player2, List.of(opposingLegendaryCreature));

        triggerNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(nonLegendaryCreature, nonCreature, saga.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingLegendaryCreature);
    }

    @Test
    void chapterIIDoesNotResolveWhenTargetLeavesGraveyard() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheNightOfTheDoctor());
        saga.setCounterCount(CounterType.LORE, 1);
        Card legendaryCreature = new YargleGluttonOfUrborg();
        harness.setGraveyard(player1, List.of(legendaryCreature));

        triggerNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(legendaryCreature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(legendaryCreature));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(legendaryCreature.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void sagaIsSacrificedOnlyAfterCounterChoiceCompletesChapterII() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheNightOfTheDoctor());
        saga.setCounterCount(CounterType.LORE, 1);
        Card legendaryCreature = new YargleGluttonOfUrborg();
        harness.setGraveyard(player1, List.of(legendaryCreature));

        triggerNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(legendaryCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.handleListChoice(player1, "Put a vigilance counter on it");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga)
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isEqualTo(legendaryCreature);
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard())
                .doesNotContain(legendaryCreature);
    }

    private void assertChapterIICounter(String choiceLabel, CounterType counterType, Keyword keyword) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheNightOfTheDoctor());
        saga.setCounterCount(CounterType.LORE, 1);
        Card legendaryCreature = new YargleGluttonOfUrborg();
        Card nonLegendaryCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(legendaryCreature, nonLegendaryCreature));

        triggerNextChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(legendaryCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(legendaryCreature.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, choiceLabel);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(legendaryCreature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(counterType)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, keyword)).isTrue();
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
    }
}
