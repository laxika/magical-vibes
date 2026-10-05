package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlunderTheTrollshaws.class})
class PlunderTheTrollshawsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting from hand draws one card")
    void castingFromHandDrawsOneCard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new PlunderTheTrollshaws()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInGraveyard(player1, "Plunder the Trollshaws");
    }

    @Test
    @DisplayName("Casting from the graveyard with flashback draws two cards and exiles the spell")
    void flashbackDrawsTwoCardsAndExilesSpell() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setGraveyard(player1, List.of(new PlunderTheTrollshaws()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        harness.assertNotInGraveyard(player1, "Plunder the Trollshaws");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Plunder the Trollshaws"));
    }

    @Test
    @DisplayName("The same card draws one from hand then two with flashback")
    void castingFromHandThenGraveyardDrawsThreeCards() {
        PlunderTheTrollshaws spell = new PlunderTheTrollshaws();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(spell));
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Flashback cannot be paid with only the normal casting cost")
    void flashbackRequiresItsFullCost() {
        PlunderTheTrollshaws spell = new PlunderTheTrollshaws();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }
}
