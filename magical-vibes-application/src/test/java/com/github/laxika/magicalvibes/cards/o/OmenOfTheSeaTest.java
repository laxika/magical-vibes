package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({OmenOfTheSea.class, NyxbornCourser.class})
class OmenOfTheSeaTest extends BaseCardTest {

    @Test
    void enteringBattlefieldScriesTwoThenDrawsACard() {
        Card firstCard = new NyxbornCourser();
        Card secondCard = new NyxbornCourser();
        Card thirdCard = new NyxbornCourser();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.castFromHand(player1, new OmenOfTheSea(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(firstCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard, firstCard);
    }

    @Test
    void sacrificesToScryTwo() {
        Card firstCard = new NyxbornCourser();
        Card secondCard = new NyxbornCourser();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheSea());
        addOmenMana();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(omen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(omen.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(firstCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard);
    }

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        Card card = new NyxbornCourser();
        harness.setLibrary(player1, List.of(card));
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        harness.castFromHand(player1, new OmenOfTheSea(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void entryTriggerStillScriesAndDrawsAfterSacrificingOmenInResponse() {
        Card firstCard = new NyxbornCourser();
        Card secondCard = new NyxbornCourser();
        Card thirdCard = new NyxbornCourser();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.castFromHand(player1, new OmenOfTheSea(), "{1}{U}");
        harness.passBothPriorities();
        addOmenMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard, secondCard, firstCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(thirdCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard, firstCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void activatedScryWithOneCardDoesNotDraw() {
        Card card = new NyxbornCourser();
        harness.setLibrary(player1, List.of(card));
        harness.addToBattlefield(player1, new OmenOfTheSea());
        addOmenMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addOmenMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
