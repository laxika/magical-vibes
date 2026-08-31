package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Avarax.class, GrizzlyBears.class})
class AvaraxTest extends BaseCardTest {

    @Test
    @DisplayName("Avarax offers to search for another Avarax when it enters")
    void entersWithOptionalNamedSearch() {
        setupAndCast();
        setLibrary(new Avarax(), new GrizzlyBears());

        resolveToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Avarax");
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.assertInHand(player1, "Avarax");
    }

    @Test
    @DisplayName("Avarax's may search can be declined")
    void canDeclineNamedSearch() {
        setupAndCast();
        setLibrary(new Avarax(), new GrizzlyBears());

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Avarax's firebreathing boost expires at end of turn")
    void firebreathingBoostExpiresAtEndOfTurn() {
        Permanent avarax = addReadyAvarax(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(avarax.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(avarax.getPowerModifier()).isEqualTo(0);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Avarax()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
    }

    private void resolveToMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }

    private Permanent addReadyAvarax(Player player) {
        return addCreatureReady(player, new Avarax());
    }
}
