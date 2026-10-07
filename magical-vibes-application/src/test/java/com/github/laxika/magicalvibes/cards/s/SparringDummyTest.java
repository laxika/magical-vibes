package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparringDummy.class, AirbendingLesson.class, Forest.class, RestInPeace.class, BruvacTheGrandiloquent.class})
class SparringDummyTest extends BaseCardTest {

    @Test
    @DisplayName("Mills a land and may return it to hand")
    void returnsMilledLandToHandWhenAccepted() {
        Card land = new Forest();
        setUpDummy(land);

        activateDummy();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining the land return leaves it in the graveyard")
    void declinesMilledLandReturn() {
        Card land = new Forest();
        setUpDummy(land);

        activateDummy();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gains 2 life when the milled card is a Lesson")
    void gainsLifeForMilledLesson() {
        Card lesson = new AirbendingLesson();
        setUpDummy(lesson);

        activateDummy();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lesson);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not return or gain life for another milled nonland card")
    void doesNothingForOtherNonlandCard() {
        Card creature = new SparringDummy();
        setUpDummy(creature);

        activateDummy();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An empty library gives neither a land choice nor life")
    void emptyLibraryDoesNothing() {
        setUpDummy(new Forest());
        harness.setLibrary(player1, List.of());

        activateDummy();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A Lesson milled into exile still gains 2 life")
    void gainsLifeForLessonExiledInsteadOfGoingToGraveyard() {
        Card lesson = new AirbendingLesson();
        setUpDummy(lesson);
        harness.addToBattlefield(player2, new RestInPeace());

        activateDummy();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(lesson);
        assertThat(gd.findExiledCard(lesson.getId())).isNotNull();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A land milled into exile may still be put into hand")
    void returnsLandExiledInsteadOfGoingToGraveyard() {
        Card land = new Forest();
        setUpDummy(land);
        harness.addToBattlefield(player2, new RestInPeace());

        activateDummy();

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    @DisplayName("When milling is doubled, the controller may choose the second land")
    void mayReturnSecondLandWhenMillingIsDoubled() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        setUpDummy(firstLand);
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.addToBattlefield(player2, new BruvacTheGrandiloquent());

        activateDummy();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstLand, secondLand);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(secondLand).doesNotContain(firstLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstLand).doesNotContain(secondLand);
        harness.assertLife(player1, 20);
    }

    private void setUpDummy(Card topCard) {
        harness.forceActivePlayer(player1);
        Permanent dummy = harness.addToBattlefieldAndReturn(player1, new SparringDummy());
        dummy.setSummoningSick(false);
        harness.setLibrary(player1, List.of(topCard));
    }

    private void activateDummy() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
