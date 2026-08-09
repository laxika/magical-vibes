package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BraidwoodSextantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and searches a basic land into its controller's hand")
    void sacrificesAndSearchesBasicLandToHand() {
        harness.addToBattlefield(player1, new BraidwoodSextant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Braidwood Sextant");
        harness.assertInGraveyard(player1, "Braidwood Sextant");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        Card chosen = search.params().cards().getFirst();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(chosen.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).doesNotContain(chosen.getId());
    }
}
