package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LotusBloom.class})
class LotusBloomTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Lotus Bloom with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        LotusBloom card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast")
    void lastCounterOffersFreeCast() {
        LotusBloom card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lotus Bloom");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Tapping and sacrificing Lotus Bloom adds three mana of the chosen color")
    void sacrificeAddsThreeManaOfChosenColor() {
        LotusBloom lotusBloom = new LotusBloom();
        harness.addToBattlefield(player1, lotusBloom);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Lotus Bloom");
        harness.assertInGraveyard(player1, "Lotus Bloom");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private LotusBloom suspendCard() {
        LotusBloom card = new LotusBloom();
        harness.setHand(player1, List.of(card));
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
