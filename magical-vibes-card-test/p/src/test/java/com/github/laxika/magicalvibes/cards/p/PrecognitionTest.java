package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Precognition.class, CanyonWildcat.class, LowlandGiant.class, Forest.class})
class PrecognitionTest extends BaseCardTest {

    private void setOpponentLibrary() {
        harness.setLibrary(player2, List.of(new CanyonWildcat(), new LowlandGiant(), new Forest()));
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

        assertThat(opponentLibraryNames()).containsExactly("Lowland Giant", "Forest", "Canyon Wildcat");
    }

    @Test
    @DisplayName("Looks at the opponent's current top card when the ability resolves")
    void usesCurrentTopCardAtResolution() {
        harness.addToBattlefield(player1, new Precognition());
        setOpponentLibrary();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLibrary(player2, List.of(new LowlandGiant(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(opponentLibraryNames()).containsExactly("Forest", "Lowland Giant");
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
        assertThat(opponentLibraryNames()).containsExactly("Canyon Wildcat", "Lowland Giant", "Forest");
    }

    @Test
    @DisplayName("An empty opponent library presents no choice")
    void emptyLibraryNoChoice() {
        harness.addToBattlefield(player1, new Precognition());
        harness.setLibrary(player2, List.of());

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
        assertThat(opponentLibraryNames()).containsExactly("Canyon Wildcat", "Lowland Giant", "Forest");
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

    @Test
    @DisplayName("The controller can decline looking without learning the top card")
    void mayDeclineLookingBeforeTopCardIsDisclosed() {
        harness.addToBattlefield(player1, new Precognition());
        setOpponentLibrary();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.clearMessages();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        var choice = (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.description()).doesNotContain("Canyon Wildcat");
        assertThat(harness.getConn1().getMessagesContaining("Canyon Wildcat")).isEmpty();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentLibraryNames()).containsExactly("Canyon Wildcat", "Lowland Giant", "Forest");
    }

    @Test
    @DisplayName("Looking does not require moving the card to the bottom")
    void mayLookAndThenDeclineBottoming() {
        harness.addToBattlefield(player1, new Precognition());
        setOpponentLibrary();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(opponentLibraryNames()).containsExactly("Canyon Wildcat", "Lowland Giant", "Forest");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        var choice = (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.description()).contains("Canyon Wildcat");

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentLibraryNames()).containsExactly("Canyon Wildcat", "Lowland Giant", "Forest");
    }
}
