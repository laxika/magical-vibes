package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlorianVoldarenScion.class, GrizzlyBears.class, Shock.class})
class FlorianVoldarenScionTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at as many cards as life opponents lost, exiles one, and randomizes the rest to the bottom")
    void exilesOneAndPutsRestOnBottomRandomly() {
        Card exiled = new GrizzlyBears();
        Card bottom = new Shock();
        Card untouched = new GrizzlyBears();
        setupFlorianAndDealTwoDamage(exiled, bottom, untouched);

        PendingInteraction.LibrarySearch search = resolveFlorianTrigger();

        assertThat(search.params().cards()).containsExactly(exiled, bottom);
        assertThat(search.params().destination())
                .isEqualTo(LibrarySearchDestination.EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
        assertThat(gd.exilePlayPermissions.get(exiled.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(exiled.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, bottom);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("May play the selected exiled card this turn")
    void mayPlaySelectedCardThisTurn() {
        Card exiled = new GrizzlyBears();
        setupFlorianAndDealTwoDamage(exiled, new Shock(), new GrizzlyBears());
        resolveFlorianTrigger();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiled);
    }

    @Test
    @DisplayName("Does nothing when opponents lost no life this turn")
    void doesNothingWhenOpponentsLostNoLife() {
        Card top = new GrizzlyBears();
        harness.addToBattlefield(player1, new FlorianVoldarenScion());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(top);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    private void setupFlorianAndDealTwoDamage(Card... topCards) {
        harness.addToBattlefield(player1, new FlorianVoldarenScion());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(topCards));

        harness.setHand(player1, List.of(new Shock()));
        harness.setLife(player2, 20);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private PendingInteraction.LibrarySearch resolveFlorianTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
