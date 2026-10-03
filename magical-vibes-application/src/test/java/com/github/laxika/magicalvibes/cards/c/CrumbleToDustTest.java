package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrumbleToDust.class, Plains.class, Wasteland.class})
class CrumbleToDustTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target nonbasic land and selected same-name cards")
    void exilesTargetAndSelectedCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Wasteland());
        Wasteland handCopy = new Wasteland();
        Wasteland graveyardCopy = new Wasteland();
        Wasteland libraryCopy = new Wasteland();
        Plains remainingHand = new Plains();
        Plains remainingLibrary = new Plains();

        harness.setHand(player2, List.of(handCopy, remainingHand));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy, remainingLibrary));
        harness.setHand(player1, List.of(new CrumbleToDust()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId(), graveyardCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(target.getCard(), handCopy, graveyardCopy)
                .doesNotContain(libraryCopy);
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(remainingHand)
                .doesNotContain(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .doesNotContain(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId()))
                .contains(libraryCopy, remainingLibrary);
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new CrumbleToDust()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonbasic land");
    }

    @Test
    @DisplayName("Can exile all matching cards, including library copies, without affecting other permanents")
    void exilesAllMatchingCardsFromAllThreeZones() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Wasteland());
        Permanent otherLand = harness.addToBattlefieldAndReturn(player2, new Wasteland());
        Wasteland handCopy = new Wasteland();
        Wasteland graveyardCopy = new Wasteland();
        Wasteland libraryCopy = new Wasteland();
        Wasteland castersCopy = new Wasteland();
        Plains otherLibraryCard = new Plains();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy, otherLibraryCard));
        harness.setGraveyard(player1, List.of(castersCopy));
        harness.setHand(player1, List.of(new CrumbleToDust()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(handCopy.getId(), graveyardCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(target.getCard(), handCopy, graveyardCopy, libraryCopy);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(otherLibraryCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherLand).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(castersCopy);
    }

    @Test
    @DisplayName("Can choose zero matching cards from all three zones")
    void canChooseZeroCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Wasteland());
        Wasteland handCopy = new Wasteland();
        Wasteland graveyardCopy = new Wasteland();
        Wasteland libraryCopy = new Wasteland();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setHand(player1, List.of(new CrumbleToDust()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exiles the land and finishes when there are no matching cards")
    void resolvesWithoutMatchingCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Wasteland());
        Plains otherCard = new Plains();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(otherCard));
        harness.setHand(player1, List.of(new CrumbleToDust()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Crumble to Dust");
    }

    @Test
    @DisplayName("Does not search or exile copies when the target leaves before resolution")
    void doesNotSearchWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Wasteland());
        Wasteland handCopy = new Wasteland();
        Wasteland libraryCopy = new Wasteland();
        harness.setHand(player2, List.of(handCopy));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(libraryCopy));
        harness.setHand(player1, List.of(new CrumbleToDust()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.activateAbility(player2, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCopy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Crumble to Dust");
    }
}
