package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrecognitionTest extends BaseCardTest {

    private void setOpponentLibrary() {
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant(), new Forest())));
    }

    private List<String> opponentLibraryNames() {
        return gd.playerDecks.get(player2.getId()).stream().map(Card::getName).toList();
    }

    @Test
    @DisplayName("Accepting the may puts the looked-at top card on the bottom of the opponent's library")
    void acceptBottomsTopCard() {
        harness.addToBattlefield(player1, new Precognition());
        setOpponentLibrary();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(opponentLibraryNames()).containsExactly("Hill Giant", "Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may leaves the opponent's library untouched")
    void declineLeavesLibraryUntouched() {
        harness.addToBattlefield(player1, new Precognition());
        setOpponentLibrary();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentLibraryNames()).containsExactly("Grizzly Bears", "Hill Giant", "Forest");
    }

    @Test
    @DisplayName("An empty opponent library presents no choice")
    void emptyLibraryNoChoice() {
        harness.addToBattlefield(player1, new Precognition());
        harness.setLibrary(player2, new ArrayList<>());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Precognition());
        setOpponentLibrary();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentLibraryNames()).containsExactly("Grizzly Bears", "Hill Giant", "Forest");
    }

    @Test
    @DisplayName("The controller cannot be chosen as the target")
    void controllerIsNotALegalTarget() {
        harness.addToBattlefield(player1, new Precognition());
        setOpponentLibrary();

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
