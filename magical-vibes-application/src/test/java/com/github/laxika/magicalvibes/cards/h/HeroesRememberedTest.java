package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroesRemembered.class})
class HeroesRememberedTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Heroes Remembered gains 20 life")
    void castingGainsTwentyLife() {
        HeroesRemembered card = new HeroesRemembered();
        harness.setHand(player1, List.of(card));
        harness.setLife(player1, 5);
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertInGraveyard(player1, "Heroes Remembered");
    }

    @Test
    @DisplayName("Suspend exiles Heroes Remembered with ten time counters and later offers a free cast")
    void suspendOffersFreeCastAfterTenUpkeeps() {
        HeroesRemembered card = new HeroesRemembered();
        harness.setHand(player1, List.of(card));
        harness.setLife(player1, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 10);

        for (int i = 0; i < 10; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertInGraveyard(player1, "Heroes Remembered");
    }
}
