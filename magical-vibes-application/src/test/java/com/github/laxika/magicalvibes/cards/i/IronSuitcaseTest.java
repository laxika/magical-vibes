package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronSuitcase.class})
class IronSuitcaseTest extends BaseCardTest {

    @Test
    void enteringBattlefieldScriesTwo() {
        List<Card> library = List.of(new IronSuitcase(), new IronSuitcase(), new IronSuitcase());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new IronSuitcase()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.get(0), library.get(1));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(2), library.get(0));
    }

    @Test
    void activatesAsFlyingConstructUntilEndOfTurn() {
        Permanent suitcase = harness.addToBattlefieldAndReturn(player1, new IronSuitcase());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suitcase)).isTrue();
        assertThat(gqs.isArtifact(suitcase)).isTrue();
        assertThat(gqs.getEffectivePower(gd, suitcase)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, suitcase)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, suitcase, CardSubtype.CONSTRUCT)).isTrue();
        assertThat(gqs.hasKeyword(gd, suitcase, Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, suitcase)).isFalse();
        assertThat(gqs.isArtifact(suitcase)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, suitcase, CardSubtype.CONSTRUCT)).isFalse();
        assertThat(gqs.hasKeyword(gd, suitcase, Keyword.FLYING)).isFalse();
    }

    @Test
    void canKeepBothScryCardsOnTopInReverseOrder() {
        List<Card> library = List.of(new IronSuitcase(), new IronSuitcase(), new IronSuitcase());
        harness.setLibrary(player1, library);
        harness.enterBattlefieldAndReturn(player1, new IronSuitcase());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.get(0), library.get(1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(0), library.get(2));
    }

    @Test
    void canPutBothScryCardsOnBottomInChosenOrder() {
        List<Card> library = List.of(new IronSuitcase(), new IronSuitcase(), new IronSuitcase());
        harness.setLibrary(player1, library);
        harness.enterBattlefieldAndReturn(player1, new IronSuitcase());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), library.get(1), library.get(0));
    }

    @Test
    void scriesOnlyAvailableCardWhenLibraryHasOneCard() {
        Card libraryCard = new IronSuitcase();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.enterBattlefieldAndReturn(player1, new IronSuitcase());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(libraryCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void enteringWithEmptyLibraryNeedsNoScryChoice() {
        harness.setLibrary(player1, List.of());
        Permanent suitcase = harness.enterBattlefieldAndReturn(player1, new IronSuitcase());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(suitcase);
    }

    @Test
    void tappedSuitcaseCanAnimateDuringOpponentsTurnWithoutAnimatingOtherSuitcases() {
        Permanent suitcase = harness.addToBattlefieldAndReturn(player1, new IronSuitcase());
        Permanent otherSuitcase = harness.addToBattlefieldAndReturn(player1, new IronSuitcase());
        Permanent opposingSuitcase = harness.addToBattlefieldAndReturn(player2, new IronSuitcase());
        suitcase.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.isCreature(gd, suitcase)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suitcase)).isTrue();
        assertThat(gqs.getEffectivePower(gd, suitcase)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, suitcase)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, suitcase, Keyword.FLYING)).isTrue();
        assertThat(suitcase.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, otherSuitcase)).isFalse();
        assertThat(gqs.isCreature(gd, opposingSuitcase)).isFalse();
    }
}
