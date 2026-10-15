package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrabenInspector.class})
class ThrabenInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("When Thraben Inspector enters, one Clue token is created")
    void etbCreatesOneClueToken() {
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        Permanent clue = clues.getFirst();
        assertThat(clue.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clue.getCard().getSubtypes()).contains(CardSubtype.CLUE);
        assertThat(clue.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Clue for {2} draws a card")
    void clueSacrificeDrawsCard() {
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.setLibrary(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIdx = gd.playerBattlefields.get(player1.getId()).indexOf(clue);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, clueIdx, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Investigate waits for its trigger to resolve and creates a Clue only for its controller")
    void investigateUsesTheEnteringCreaturesController() {
        harness.enterBattlefieldAndReturn(player2, new ThrabenInspector());

        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("A tapped Clue can be sacrificed, with the card drawn only on resolution")
    void tappedClueIsSacrificedAsAnActivationCost() {
        harness.enterBattlefieldAndReturn(player1, new ThrabenInspector());
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        ThrabenInspector drawnCard = new ThrabenInspector();
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent clue = findPermanent(player1, "Clue");
        clue.tap();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
