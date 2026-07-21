package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HourOfEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=2 prompts for X target creature cards from your graveyard")
    void castingPromptsGraveyardChoice() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card plains = new Plains();
        harness.setGraveyard(player1, List.of(bears1, bears2, plains));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7); // {X}{X}{U}{U}{U} with X=2 costs 4 generic + UUU

        harness.castSorcery(player1, 0, 2);

        // Awaiting the X-scaled graveyard choice; spell not yet on the stack
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        // Only the controller's creature cards are valid (Plains excluded)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactlyInAnyOrder(bears1.getId(), bears2.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolving exiles the creatures and creates a 4/4 black Zombie copy of each")
    void resolvingCreatesFourFourBlackZombieCopies() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));
        harness.passBothPriorities();

        // Both creatures exiled (only Hour of Eternity itself remains in the graveyard)
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Hour of Eternity");

        // Two token copies of Grizzly Bears on the battlefield (proves they are copies, not generic Zombies)
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
            assertThat(token.getCard().getPower()).isEqualTo(4);
            assertThat(token.getCard().getToughness()).isEqualTo(4);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            // Zombie added in addition to the copied card's other creature types
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.ZOMBIE);
        }
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution creates fewer copies")
    void targetRemovedBeforeResolutionCreatesFewerCopies() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1, bears2));
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        // One target leaves the graveyard before resolution
        gd.playerGraveyards.get(player1.getId()).removeIf(c -> c.getId().equals(bears1.getId()));

        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting with X greater than the number of creature cards in the graveyard is illegal")
    void xGreaterThanCreatureCountThrows() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears())); // only one creature card
        harness.setHand(player1, List.of(new HourOfEternity()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature cards in graveyard");
    }
}
