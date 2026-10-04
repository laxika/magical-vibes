package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirjaJudgeOfValor.class, GrizzlyBears.class, Shock.class})
class FirjaJudgeOfValorTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at the top three cards on its controller's second spell and puts one in hand")
    void looksAtTopThreeOnSecondSpell() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        addCreatureReady(player1, new FirjaJudgeOfValor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mustChooseOneCardWhenCardsAreAvailable() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        addCreatureReady(player1, new FirjaJudgeOfValor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
    }

    @Test
    void takesTheOnlyCardWithoutAChoice() {
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        addCreatureReady(player1, new FirjaJudgeOfValor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(only);
    }

    @Test
    void emptyLibraryDoesNotCreateAChoiceOrCauseDrawLoss() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new FirjaJudgeOfValor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void thirdSpellDoesNotTriggerAgain() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        addCreatureReady(player1, new FirjaJudgeOfValor());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
    }

    @Test
    void countsFirstSpellCastBeforeFirjaEntered() {
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        addCreatureReady(player1, new FirjaJudgeOfValor());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
    }

    @Test
    void opponentsSpellsDoNotTriggerFirja() {
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        addCreatureReady(player1, new FirjaJudgeOfValor());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(only);
    }

    @Test
    void controllersSecondSpellTriggersDuringOpponentsTurn() {
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        addCreatureReady(player1, new FirjaJudgeOfValor());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
    }

    @Test
    void firjaCastAsFirstSpellCountsForTheNextSpell() {
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new FirjaJudgeOfValor(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(only);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
    }

    @Test
    void firjaCastAsSecondSpellDoesNotTriggerForItselfOrThirdSpell() {
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new Shock(), new FirjaJudgeOfValor(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(only);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
