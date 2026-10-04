package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Scorchmark;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinGathering.class, Scorchmark.class})
class GoblinGatheringTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 1/1 red Goblin tokens with no matching cards in the graveyard")
    void createsBaseTokens() {
        castGoblinGathering();

        assertGoblinTokens(2);
    }

    @Test
    @DisplayName("Creates one additional token for each Goblin Gathering in the controller's graveyard")
    void createsAdditionalTokenPerMatchingGraveyardCard() {
        harness.setGraveyard(player1, List.of(new GoblinGathering(), new GoblinGathering()));

        castGoblinGathering();

        assertGoblinTokens(4);
        harness.assertInGraveyard(player1, "Goblin Gathering");
    }

    @Test
    @DisplayName("Counts only Goblin Gatherings in the controller's graveyard")
    void ignoresOpponentAndOtherCards() {
        harness.setGraveyard(player1, List.of(new GoblinGathering()));
        harness.setGraveyard(player2, List.of(new GoblinGathering(), new GoblinGathering()));

        castGoblinGathering();

        assertGoblinTokens(3);
    }

    @Test
    @DisplayName("Ignores cards with other names in the controller's graveyard")
    void ignoresUnrelatedGraveyardCards() {
        harness.setGraveyard(player1, List.of(new Scorchmark(), new GoblinGathering()));

        castGoblinGathering();

        assertGoblinTokens(3);
    }

    @Test
    @DisplayName("Counts matching cards added to the graveyard before resolution")
    void countsGraveyardAtResolution() {
        harness.setHand(player1, List.of(new GoblinGathering()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);

        harness.setGraveyard(player1, List.of(new GoblinGathering(), new GoblinGathering()));
        harness.passBothPriorities();

        assertGoblinTokens(4);
    }

    @Test
    @DisplayName("Counts a previously resolved Gathering but not the resolving spell itself")
    void successiveGatheringsCreateTwoThenThreeTokens() {
        castGoblinGathering();
        assertGoblinTokens(2);

        castGoblinGathering();

        assertGoblinTokens(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    private void castGoblinGathering() {
        harness.setHand(player1, List.of(new GoblinGathering()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void assertGoblinTokens(int expectedCount) {
        List<Permanent> tokens = findPermanents(player1, "Goblin");
        assertThat(tokens).hasSize(expectedCount);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        });
    }
}
