package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DovinsDismissal.class, DovinArchitectOfLaw.class, Forest.class, GrizzlyBears.class})
class DovinsDismissalTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a tapped creature on top of its owner's library and searches the graveyard")
    void putsTappedCreatureOnTopAndSearchesGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        Card libraryCard = new Forest();
        Card dovin = new DovinArchitectOfLaw();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(dovin));

        cast(target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(dovin.getId()));

        harness.assertInHand(player1, "Dovin, Architect of Law");
        harness.assertNotInGraveyard(player1, "Dovin, Architect of Law");
    }

    @Test
    @DisplayName("Can decline the optional search without choosing a target")
    void canDeclineSearchWithoutChoosingTarget() {
        Card dovin = new DovinArchitectOfLaw();
        harness.setGraveyard(player1, List.of(dovin));

        harness.setHand(player1, List.of(new DovinsDismissal()));
        addMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dovin, Architect of Law");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DovinsDismissal()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Searches the library for Dovin, Architect of Law")
    void searchesLibraryForDovin() {
        Card dovin = new DovinArchitectOfLaw();
        harness.setLibrary(player1, List.of(dovin));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        cast(target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice search =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(search.validCardIds()).containsExactly(dovin.getId());
        harness.handleMultipleCardsChosen(player1, List.of(dovin.getId()));

        harness.assertInHand(player1, "Dovin, Architect of Law");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void cast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DovinsDismissal()));
        addMana();
        harness.castInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
