package com.github.laxika.magicalvibes.cards.e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(EndlessSwarm.class)
class EndlessSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 1/1 green Snake for each card in hand and applies Epic")
    void createsSnakesForCardsInHandAndAppliesEpic() {
        harness.setHand(player1, List.of(new EndlessSwarm(), new EndlessSwarm(), new EndlessSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(2)
                .allMatch(token -> token.getCard().isToken()
                && token.getEffectivePower() == 1
                && token.getEffectiveToughness() == 1
                && token.getCard().getColor() == CardColor.GREEN
                && token.getCard().getSubtypes().contains(CardSubtype.SNAKE));
        assertThat(gd.playersCantCastSpellsForRestOfGame).contains(player1.getId());

        harness.setHand(player1, List.of(new EndlessSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Still applies Epic when casting leaves no cards in hand")
    void appliesEpicWhenHandIsEmpty() {
        harness.setHand(player1, List.of(new EndlessSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).isEmpty();
        assertThat(gd.playersCantCastSpellsForRestOfGame).contains(player1.getId());
    }

    @Test
    @DisplayName("Copies the token creation at the beginning of the controller's upkeep")
    void copiesTokenCreationAtUpkeep() {
        harness.setHand(player1, List.of(new EndlessSwarm(), new EndlessSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Snake")).hasSize(1);

        harness.setHand(player1, List.of(new EndlessSwarm(), new EndlessSwarm()));
        harness.setLibrary(player1, List.of());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(5);
    }
}
