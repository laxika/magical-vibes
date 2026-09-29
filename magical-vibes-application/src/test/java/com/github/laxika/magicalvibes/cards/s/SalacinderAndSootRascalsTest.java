package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.cards.t.TakeInventory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SalacinderAndSootRascals.class, AirElemental.class, GrizzlyBears.class,
        RiteOfFlame.class, TakeInventory.class})
class SalacinderAndSootRascalsTest extends BaseCardTest {

    @Test
    void entersWithAChoiceBetweenRiteOfFlameAndTakeInventory() {
        harness.enterBattlefieldAndReturn(player1, new SalacinderAndSootRascals());
        harness.passBothPriorities();

        chooseSpellbookCard("Rite of Flame");

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Rite of Flame");
    }

    @Test
    void triggersWhenControllerCastsAnElementalSpell() {
        harness.addToBattlefield(player1, new SalacinderAndSootRascals());
        harness.setHand(player1, List.of(new AirElemental()));

        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        chooseSpellbookCard("Take Inventory");

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Take Inventory");
    }

    @Test
    void doesNotTriggerForANonElementalSpell() {
        harness.addToBattlefield(player1, new SalacinderAndSootRascals());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class))
                .isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void chooseSpellbookCard(String cardName) {
        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice).isNotNull();

        var selectedCard = choice.cards().stream()
                .filter(card -> card.getName().equals(cardName))
                .findFirst()
                .orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(selectedCard.getId()));
    }
}
