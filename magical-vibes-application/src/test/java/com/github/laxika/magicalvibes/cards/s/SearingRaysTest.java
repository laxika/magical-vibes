package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearingRaysTest extends BaseCardTest {

    private static Card createCreature(String name, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setColor(colors[0]);
        card.setColors(List.of(colors));
        return card;
    }

    private static Card createArtifact(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setColor(color);
        card.setColors(List.of(color));
        return card;
    }

    private void castSearingRays() {
        harness.setHand(player1, List.of(new SearingRays()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving Searing Rays prompts for a color")
    void resolvingPromptsForColor() {
        castSearingRays();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Deals damage to each player based on that player's creatures of the chosen color")
    void dealsPerPlayerCreatureCount() {
        harness.addToBattlefield(player1, createCreature("Red Creature", CardColor.RED));
        harness.addToBattlefield(player1, createCreature("Red Green Creature", CardColor.RED, CardColor.GREEN));
        harness.addToBattlefield(player1, createArtifact("Red Artifact", CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Red Creature", CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Green Creature", CardColor.GREEN));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSearingRays();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A color with no creatures deals no damage")
    void noMatchingCreaturesDealNoDamage() {
        harness.addToBattlefield(player1, createCreature("Green Creature", CardColor.GREEN));
        harness.addToBattlefield(player2, createCreature("White Creature", CardColor.WHITE));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSearingRays();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
