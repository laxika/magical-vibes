package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArgothianEnchantress;
import com.github.laxika.magicalvibes.cards.c.CradleGuard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreaterGood.class, CradleGuard.class, ArgothianEnchantress.class, Forest.class})
class GreaterGoodTest extends BaseCardTest {

    // ===== Draws equal to sacrificed creature's power, then discards three =====

    @Test
    @DisplayName("Sacrificing a 4-power creature draws four cards, then discards three")
    void drawsEqualToPowerThenDiscardsThree() {
        harness.addToBattlefield(player1, new GreaterGood());
        harness.addToBattlefield(player1, new CradleGuard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, forests(6));

        // Only one creature on the battlefield → auto-sacrifice
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Drew four, so hand is 4 and a discard choice is now pending
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();

        // Discard the required three
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Zero-power creature draws nothing but still discards three =====

    @Test
    @DisplayName("Sacrificing a 0-power creature draws nothing but still discards three")
    void zeroPowerDrawsNothingButStillDiscardsThree() {
        harness.addToBattlefield(player1, new GreaterGood());
        harness.addToBattlefield(player1, new ArgothianEnchantress());
        harness.setLibrary(player1, forests(6));
        harness.setHand(player1, forests(4));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No cards drawn: hand is still the original four
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== The sacrificed creature ends up in the graveyard =====

    @Test
    @DisplayName("The sacrificed creature is put into the graveyard")
    void sacrificedCreatureGoesToGraveyard() {
        harness.addToBattlefield(player1, new GreaterGood());
        harness.addToBattlefield(player1, new CradleGuard());
        harness.setLibrary(player1, forests(6));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Creature gone from the battlefield, only Greater Good remains
        harness.assertNotOnBattlefield(player1, "Cradle Guard");
        harness.assertInGraveyard(player1, "Cradle Guard");
    }

    @Test
    @DisplayName("The controller chooses which creature to sacrifice when multiple are available")
    void choosesWhichCreatureToSacrifice() {
        harness.addToBattlefield(player1, new GreaterGood());
        var chosen = harness.addToBattlefieldAndReturn(player1, new CradleGuard());
        harness.addToBattlefield(player1, new ArgothianEnchantress());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, forests(6));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chosen.getCard());
        harness.assertOnBattlefield(player1, "Argothian Enchantress");

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private List<Card> forests(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new Forest())
                .toList();
    }
}
