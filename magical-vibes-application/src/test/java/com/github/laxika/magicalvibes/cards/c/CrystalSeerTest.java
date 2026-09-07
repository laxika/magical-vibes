package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CrystalSeer.class)
class CrystalSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crystal Seer triggers a top-four library reorder")
    void resolvingTriggersTopFourReorder() {
        castCrystalSeer();

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .hasSize(4);
    }

    @Test
    @DisplayName("Crystal Seer reorder changes the top four cards of its controller's library")
    void reorderChangesTopCards() {
        castCrystalSeer();

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);
        Card originalTop3 = deck.get(3);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(deck).containsSubsequence(originalTop3, originalTop2, originalTop1, originalTop0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Crystal Seer can return itself to its owner's hand")
    void returnsItselfToHand() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new CrystalSeer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(seer);
        harness.assertInHand(player1, "Crystal Seer");
    }

    private void castCrystalSeer() {
        harness.setHand(player1, List.of(new CrystalSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }
}
