package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FadingHope.class, AirElemental.class, GrizzlyBears.class, Island.class})
class FadingHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with mana value 3 or less and offers scry 1")
    void returnsLowManaValueCreatureAndScries() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castFadingHope(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Returns a creature with mana value greater than 3 without scrying")
    void returnsHighManaValueCreatureWithoutScrying() {
        harness.addToBattlefield(player2, new AirElemental());
        castFadingHope(harness.getPermanentId(player2, "Air Elemental"));

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInHand(player2, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry 1 can put the top card on the bottom")
    void scryCanPutTopCardOnBottom() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();
        castFadingHope(harness.getPermanentId(player2, "Grizzly Bears"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getLast()).isSameAs(originalTop);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fading Hope");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new FadingHope()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFadingHope(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FadingHope()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
