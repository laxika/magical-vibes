package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Avarax.class, ElvishWarrior.class})
class AvaraxTest extends BaseCardTest {

    @Test
    @DisplayName("Avarax offers to search for another Avarax when it enters")
    void entersWithOptionalNamedSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Avarax(), new ElvishWarrior()));

        resolveToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Avarax");
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Avarax");
    }

    @Test
    @DisplayName("Avarax's may search can be declined")
    void canDeclineNamedSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Avarax(), new ElvishWarrior()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Avarax's accepted search can find no matching card")
    void acceptedNamedSearchCanFindNothing() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Elvish Warrior");
        assertThat(gameLogContains("finds no cards named Avarax. Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Avarax's firebreathing boost expires at end of turn")
    void firebreathingBoostExpiresAtEndOfTurn() {
        Permanent avarax = addCreatureReady(player1, new Avarax());
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
        harness.castFromHand(player1, new Avarax(), "{3}{R}{R}");
    }

    private void resolveToMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
