package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestralTribute.class, Plains.class})
class AncestralTributeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each card in your graveyard")
    void gainsTwoLifePerCardInYourGraveyard() {
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));
        harness.setGraveyard(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.setHand(player1, List.of(new AncestralTribute()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("Gains no life with an empty graveyard")
    void gainsNoLifeWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new AncestralTribute()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Flashback repeats the life gain and exiles the spell")
    void flashbackRepeatsLifeGainAndExilesSpell() {
        AncestralTribute tribute = new AncestralTribute();
        harness.setGraveyard(player1, List.of(tribute, new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 12);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
        harness.assertNotInGraveyard(player1, "Ancestral Tribute");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ancestral Tribute"));
    }
}
