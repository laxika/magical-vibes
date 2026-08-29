package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AncientVendettaTest extends BaseCardTest {

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new AncientVendetta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void exilesUpToFourNamedCardsAcrossAllThreeZones() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();
        Card bears4 = new GrizzlyBears();
        Card bears5 = new GrizzlyBears();

        harness.setHand(player2, new ArrayList<>(List.of(bears1, bears2)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears3)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears4, bears5));

        harness.setHand(player1, List.of(new AncientVendetta()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(bears1.getId(), bears2.getId(), bears3.getId(), bears4.getId(), bears5.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Choose at most 4 cards");

        harness.handleMultipleCardsChosen(player1,
                List.of(bears1.getId(), bears2.getId(), bears3.getId(), bears4.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears5);
    }
}
