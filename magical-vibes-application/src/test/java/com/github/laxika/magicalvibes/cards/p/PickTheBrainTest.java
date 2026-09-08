package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PickTheBrainTest extends BaseCardTest {

    @Test
    @DisplayName("Without delirium, exiles one chosen nonland card")
    void withoutDeliriumExilesOneChosenCard() {
        LavaAxe first = new LavaAxe();
        LavaAxe second = new LavaAxe();
        LavaAxe graveyardCopy = new LavaAxe();
        LavaAxe libraryCopy = new LavaAxe();
        harness.setHand(player2, new ArrayList<>(List.of(first, second, new Forest())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCopy)));
        harness.setLibrary(player2, new ArrayList<>(List.of(libraryCopy, new Forest())));

        castPickTheBrain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Lava Axe");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Lava Axe", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("With delirium, exiles the chosen card and any number of same-name cards")
    void withDeliriumExilesChosenCardAndAnyNumberOfCopies() {
        LavaAxe first = new LavaAxe();
        LavaAxe second = new LavaAxe();
        LavaAxe graveyardCopy = new LavaAxe();
        LavaAxe libraryCopy = new LavaAxe();
        harness.setHand(player2, new ArrayList<>(List.of(first, second, new Forest())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new LavaAxe(), new LightningBolt(), new GrizzlyBears(), new Millstone())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(
                graveyardCopy, new LightningBolt(), new GrizzlyBears(), new Millstone())));
        harness.setLibrary(player2, new ArrayList<>(List.of(libraryCopy, new Forest())));

        castPickTheBrain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Lava Axe");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Lava Axe", "Lava Axe");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Lava Axe", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Lightning Bolt", "Grizzly Bears", "Millstone");
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new PickTheBrain()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castPickTheBrain() {
        harness.setHand(player1, List.of(new PickTheBrain()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
