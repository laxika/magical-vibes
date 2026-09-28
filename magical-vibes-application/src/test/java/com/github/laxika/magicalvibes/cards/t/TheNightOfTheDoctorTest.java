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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
