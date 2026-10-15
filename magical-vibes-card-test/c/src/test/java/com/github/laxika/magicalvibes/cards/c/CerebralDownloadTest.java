package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SurveyMechan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CerebralDownload.class, Forest.class, SurveyMechan.class})
class CerebralDownloadTest extends BaseCardTest {

    @Test
    void surveilsForControlledArtifactsThenDrawsThree() {
        Card surveiled = new Forest();
        Card keptOnTop = new Forest();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card thirdDraw = new Forest();
        harness.setLibrary(player1, List.of(surveiled, keptOnTop, firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(surveiled, keptOnTop);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptOnTop, firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveiled);
    }

    @Test
    void drawsThreeWithoutSurveillingWhenNoArtifactsAreControlled() {
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card thirdDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firstDraw, secondDraw, thirdDraw);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void completesSingleCardSurveilBeforeDrawing(boolean putIntoGraveyard) {
        Card top = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(top, second, third, fourth));
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.addToBattlefield(player2, new SurveyMechan());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, putIntoGraveyard);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(
                putIntoGraveyard ? second : top,
                putIntoGraveyard ? third : second,
                putIntoGraveyard ? fourth : third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(
                putIntoGraveyard ? List.of() : List.of(fourth));
        if (putIntoGraveyard) {
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
        } else {
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(top);
        }
    }

    @Test
    void countsArtifactsAtResolutionAndCanReorderAllSurveilledCards() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void canPutAllSurveilledCardsIntoGraveyardThenDrawThree() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card fifth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new CerebralDownload()));
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.addToBattlefield(player1, new SurveyMechan());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, fourth, fifth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }
}
