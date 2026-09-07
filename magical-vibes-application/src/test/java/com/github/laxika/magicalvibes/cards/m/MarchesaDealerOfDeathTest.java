package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchesaDealerOfDeath.class, Forest.class, GrizzlyBears.class, Shock.class})
class MarchesaDealerOfDeathTest extends BaseCardTest {

    @Test
    @DisplayName("After paying, puts one of the top two cards into hand and the other into the graveyard")
    void paysToLookAtTopTwoAndChooseOne() {
        Card kept = new Forest();
        Card discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(kept, discarded));
        harness.addToBattlefield(player1, new MarchesaDealerOfDeath());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(kept.getId(), discarded.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the payment leaves the library unchanged")
    void decliningPaymentDoesNothing() {
        Card top = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second));
        harness.addToBattlefield(player1, new MarchesaDealerOfDeath());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(top, second);
    }
}
