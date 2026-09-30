package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SevenDwarves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JewelMineOverseer.class, SevenDwarves.class, GrizzlyBears.class})
class JewelMineOverseerTest extends BaseCardTest {

    @Test
    void entersAndConjuresSevenDwarvesWithPerpetualDraw() {
        Card overseer = new JewelMineOverseer();
        harness.setHand(player1, List.of(overseer));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(8);
        assertThat(library).filteredOn(Card::getName, "Seven Dwarves").hasSize(7);

        Card conjuredDwarf = library.stream()
                .filter(card -> "Seven Dwarves".equals(card.getName()))
                .findFirst()
                .orElseThrow();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(conjuredDwarf));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    void upkeepExilesTopCardAndGrantsPlayPermissionUntilEndOfTurn() {
        Card topCard = new GrizzlyBears();
        addCreatureReady(player1, new JewelMineOverseer());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
