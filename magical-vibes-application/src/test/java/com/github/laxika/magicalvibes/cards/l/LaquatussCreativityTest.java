package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaquatussCreativity.class, Island.class, Peek.class, WoodlandDruid.class})
class LaquatussCreativityTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws their starting hand size, then discards that many cards")
    void drawsThenDiscardsStartingHandSize() {
        harness.setHand(player2, new ArrayList<>(List.of(new WoodlandDruid(), new Peek())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island(), new Island())));
        harness.setHand(player1, List.of(new LaquatussCreativity()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card.getName().equals("Island"))
                .hasSize(2);
        harness.assertInGraveyard(player2, "Woodland Druid");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    @DisplayName("The caster can target themselves")
    void canTargetController() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new LaquatussCreativity(), new WoodlandDruid(), new Peek())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island(), new Island())));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card.getName().equals("Island"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Laquatus's Creativity");
        harness.assertInGraveyard(player1, "Woodland Druid");
    }

    @Test
    @DisplayName("A target with an empty hand draws and discards nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island(), new Island())));
        harness.setHand(player1, List.of(new LaquatussCreativity()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Laquatus's Creativity cannot target a permanent")
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        harness.setHand(player1, List.of(new LaquatussCreativity()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
