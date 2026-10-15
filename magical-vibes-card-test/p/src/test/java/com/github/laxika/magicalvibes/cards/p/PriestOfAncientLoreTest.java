package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestOfAncientLore.class, PowerWordKill.class})
class PriestOfAncientLoreTest extends BaseCardTest {

    @Test
    void entersGainsLifeAndDrawsCard() {
        Card drawnCard = new PriestOfAncientLore();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PriestOfAncientLore()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void benefitsWaitForTheSingleEnterTriggerToResolve() {
        Card drawnCard = new PriestOfAncientLore();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player1, new PriestOfAncientLore());

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersUnderOpponentControlBenefitsOnlyThatController() {
        Card drawnCard = new PriestOfAncientLore();
        Card otherLibraryCard = new PriestOfAncientLore();
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(otherLibraryCard));
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new PriestOfAncientLore());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 11);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibraryCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    void triggerStillGainsLifeAndDrawsAfterPriestIsDestroyed() {
        Card drawnCard = new PriestOfAncientLore();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLACK, 2);
        Permanent priest = harness.enterBattlefieldAndReturn(player1, new PriestOfAncientLore());

        harness.castAndResolveInstant(player2, 0, priest.getId());

        harness.assertInGraveyard(player1, "Priest of Ancient Lore");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
