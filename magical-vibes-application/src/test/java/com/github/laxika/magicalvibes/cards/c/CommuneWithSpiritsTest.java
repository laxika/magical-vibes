package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
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

@CardUsed({CommuneWithSpirits.class, Forest.class, GrizzlyBears.class, PrismaticOmen.class, Shock.class})
class CommuneWithSpiritsTest extends BaseCardTest {

    @Test
    @DisplayName("offers enchantment and land cards from the top four")
    void offersEnchantmentAndLandCards() {
        PrismaticOmen enchantment = new PrismaticOmen();
        Forest forest = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        Shock instant = new Shock();
        setUpAndCast(List.of(enchantment, forest, creature, instant));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId(), forest.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, creature, instant);
    }

    @Test
    @DisplayName("may decline and puts all four cards on the bottom")
    void mayDecline() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Shock();
        Card fourth = new Forest();
        setUpAndCast(List.of(first, second, third, fourth));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);
    }

    private void setUpAndCast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CommuneWithSpirits()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
