package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SevenDwarves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JewelMineOverseer.class, SevenDwarves.class, GrizzlyBears.class, CosisTrickster.class})
class JewelMineOverseerTest extends BaseCardTest {

    @Test
    void entersAndConjuresSevenDwarvesWithPerpetualDraw() {
        Card overseer = new JewelMineOverseer();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, overseer, "{1}{R}{W}");
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(8);
        assertThat(library).filteredOn(Card::getName, "Seven Dwarves").hasSize(7);

        Card conjuredDwarf = library.stream()
                .filter(card -> "Seven Dwarves".equals(card.getName()))
                .findFirst()
                .orElseThrow();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, conjuredDwarf, "{1}{R}");
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
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

    @Test
    void enteringShufflesOnlyOnce() {
        Permanent trickster = addCreatureReady(player2, new CosisTrickster());
        harness.setLibrary(player1, List.of(new SevenDwarves()));

        harness.castFromHand(player1, new JewelMineOverseer(), "{1}{R}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentUpkeepDoesNotExileEitherPlayersLibrary() {
        Card ownTop = new SevenDwarves();
        Card opponentTop = new SevenDwarves();
        addCreatureReady(player1, new JewelMineOverseer());
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void emptyLibraryAtUpkeepDoesNotLoseTheGame() {
        addCreatureReady(player1, new JewelMineOverseer());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void exiledCreatureCanBeCastWithItsNormalManaCost() {
        Card topCard = new SevenDwarves();
        addCreatureReady(player1, new JewelMineOverseer());
        harness.setLibrary(player1, List.of(topCard, new SevenDwarves()));
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Seven Dwarves");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void exiledCreatureCannotBeCastForFree() {
        Card topCard = new SevenDwarves();
        addCreatureReady(player1, new JewelMineOverseer());
        harness.setLibrary(player1, List.of(topCard));
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.playerManaPools.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        harness.assertNotOnBattlefield(player1, "Seven Dwarves");
    }

    @Test
    void playPermissionExpiresWhileTheCardRemainsExiled() {
        Card topCard = new SevenDwarves();
        addCreatureReady(player1, new JewelMineOverseer());
        harness.setLibrary(player1, List.of(topCard, new SevenDwarves(), new SevenDwarves()));
        harness.setLibrary(player2, List.of(new SevenDwarves(), new SevenDwarves()));
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    @Test
    void everyConjuredDwarfDrawsButAnOrdinaryDwarfDoesNot() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new JewelMineOverseer(), "{1}{R}{W}");
        resolveAllTriggers();
        List<Card> dwarves = List.copyOf(gd.playerDecks.get(player1.getId()));
        assertThat(dwarves).hasSize(7);

        for (Card dwarf : dwarves) {
            Card drawnCard = new JewelMineOverseer();
            harness.setLibrary(player1, List.of(drawnCard));
            harness.castFromHand(player1, dwarf, "{1}{R}");
            resolveAllTriggers();
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        }

        Card undrawnCard = new JewelMineOverseer();
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.castFromHand(player1, new SevenDwarves(), "{1}{R}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
    }
}
