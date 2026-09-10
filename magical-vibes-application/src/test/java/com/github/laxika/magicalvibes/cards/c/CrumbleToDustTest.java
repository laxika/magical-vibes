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

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

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
}
