package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoundFootage.class, FearOfExposure.class})
class FoundFootageTest extends BaseCardTest {

    @Test
    void controllerMayLookAtOpposingFaceDownCreatures() {
        harness.addToBattlefield(player1, new FoundFootage());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new FearOfExposure());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("Fear of Exposure"));
        assertThat(gqs.mayLookAtOpposingFaceDownCreatures(gd, player2.getId())).isFalse();
    }

    @Test
    void doesNotRevealOpposingFaceDownNoncreatures() {
        harness.addToBattlefield(player1, new FoundFootage());
        Permanent faceDownArtifact = harness.addToBattlefieldAndReturn(player2, new FearOfExposure());
        faceDownArtifact.setFaceDown(0, 0, Set.of(CardType.ARTIFACT));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).isNotEmpty()
                .noneMatch(message -> message.contains("Fear of Exposure"));
    }

    @Test
    void sacrificeAbilitySurveilsTwoThenDrawsAndSacrificesFoundFootage() {
        Permanent foundFootage = harness.addToBattlefieldAndReturn(player1, new FoundFootage());
        Card surveilledCard = new FearOfExposure();
        Card secondSurveilledCard = new FearOfExposure();
        Card drawnCard = new FearOfExposure();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(surveilledCard, secondSurveilledCard, drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(surveilledCard, secondSurveilledCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(foundFootage);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(surveilledCard, secondSurveilledCard, foundFootage.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void mayKeepBothSurveilledCardsInReverseOrderBeforeDrawing() {
        harness.addToBattlefield(player1, new FoundFootage());
        Card firstCard = new FearOfExposure();
        Card secondCard = new FearOfExposure();
        Card thirdCard = new FearOfExposure();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstCard, thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firstCard, secondCard);
    }

    @Test
    void mayPutOneSurveilledCardIntoGraveyardAndDrawTheOther() {
        harness.addToBattlefield(player1, new FoundFootage());
        Card rejectedCard = new FearOfExposure();
        Card keptCard = new FearOfExposure();
        Card thirdCard = new FearOfExposure();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(rejectedCard, keptCard, thirdCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rejectedCard).doesNotContain(keptCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard);
    }

    @Test
    void surveilsTheOnlyLibraryCardThenDrawsIt() {
        harness.addToBattlefield(player1, new FoundFootage());
        Card onlyCard = new FearOfExposure();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificeIsPaidAndLookPermissionEndsBeforeTheAbilityResolves() {
        Permanent foundFootage = harness.addToBattlefieldAndReturn(player1, new FoundFootage());
        harness.setHand(player1, List.of());
        Card firstCard = new FearOfExposure();
        Card secondCard = new FearOfExposure();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThat(gqs.mayLookAtOpposingFaceDownCreatures(gd, player1.getId())).isTrue();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(foundFootage);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(foundFootage.getCard());
        assertThat(gqs.mayLookAtOpposingFaceDownCreatures(gd, player1.getId())).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstCard, secondCard);

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
    }
}
