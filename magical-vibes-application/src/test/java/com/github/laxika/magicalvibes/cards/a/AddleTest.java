package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Addle first prompts for a color")
    void resolvingPromptsForColor() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The chosen color limits the card choice and discards exactly one matching card")
    void choosesOneCardOfChosenColor() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new Opt(), new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName())
                .containsExactly("Opt", "Grizzly Bears", "Forest");
    }

    @Test
    @DisplayName("A color with no matching card leaves the target hand unchanged")
    void noMatchingColorDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName())
                .containsExactly("Peek", "Grizzly Bears", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
