package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrowingRanksTest extends BaseCardTest {

    @Test
    @DisplayName("Populates during its controller's upkeep")
    void populatesDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new GrowingRanks());
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
    }

    @Test
    @DisplayName("Does not populate during an opponent's upkeep")
    void doesNotPopulateDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GrowingRanks());
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing during its controller's upkeep without a creature token")
    void doesNothingWithoutCreatureToken() {
        harness.addToBattlefield(player1, new GrowingRanks());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Lets its controller choose which creature token to copy")
    void choosesCreatureTokenToCopy() {
        harness.addToBattlefield(player1, new GrowingRanks());
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        harness.addToBattlefield(player1, creatureToken("Saproling Token"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent soldier = findPermanent(player1, "Soldier Token");
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(findPermanents(player1, "Soldier Token")).hasSize(2);
        assertThat(findPermanents(player1, "Saproling Token")).hasSize(1);
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
