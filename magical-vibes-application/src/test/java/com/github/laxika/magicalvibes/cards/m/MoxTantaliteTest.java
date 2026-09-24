package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoxTantalite.class})
class MoxTantaliteTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Mox Tantalite with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        MoxTantalite card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast")
    void lastCounterOffersFreeCast() {
        MoxTantalite card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mox Tantalite");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Tapping Mox Tantalite adds one mana of the chosen color")
    void tappingAddsManaOfChosenColor() {
        MoxTantalite mox = new MoxTantalite();
        harness.addToBattlefield(player1, mox);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private MoxTantalite suspendCard() {
        MoxTantalite card = new MoxTantalite();
        harness.setHand(player1, List.of(card));
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
