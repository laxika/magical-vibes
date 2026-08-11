package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaintedPactTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the first unique exiled card into hand and stops")
    void putsAcceptedCardIntoHandAndStops() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        harness.setHand(player1, List.of(new TaintedPact()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).contains("Tainted Pact");
    }

    @Test
    @DisplayName("Continues exiling when a unique card is declined")
    void continuesAfterDecliningUniqueCard() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        harness.setHand(player1, List.of(new TaintedPact()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).contains("Tainted Pact");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Stops on a duplicate name without offering it to hand")
    void stopsOnDuplicateName() {
        Card first = new GrizzlyBears();
        Card duplicate = new GrizzlyBears();
        Card remaining = new Shock();
        harness.setHand(player1, List.of(new TaintedPact()));
        harness.setLibrary(player1, List.of(first, duplicate, remaining));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, duplicate);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName).contains("Tainted Pact");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
    }
}
