package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MercenaryKnight.class, Forest.class, GrizzlyBears.class})
class MercenaryKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card keeps Mercenary Knight on the battlefield")
    void discardingCreatureKeepsKnight() {
        castKnightWithCreatureInHand();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0); // discard the creature

        harness.assertOnBattlefield(player1, "Mercenary Knight");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the discard sacrifices Mercenary Knight")
    void decliningSacrificesKnight() {
        castKnightWithCreatureInHand();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Mercenary Knight");
        harness.assertInGraveyard(player1, "Mercenary Knight");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards are offered for the discard")
    void onlyCreatureCardsCanBeDiscarded() {
        harness.castFromHand(player1, new MercenaryKnight(), "{2}{B}");
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Mercenary Knight");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no creature cards in hand")
    void autoSacrificesWithNoCreatureInHand() {
        harness.castFromHand(player1, new MercenaryKnight(), "{2}{B}");
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Mercenary Knight");
        harness.assertInGraveyard(player1, "Mercenary Knight");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    /**
     * Casts Mercenary Knight with a creature (Grizzly Bears) in hand, resolves through
     * to the may ability prompt.
     */
    private void castKnightWithCreatureInHand() {
        harness.castFromHand(player1, new MercenaryKnight(), "{2}{B}");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
