package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GrizzlyFate.class, GrizzlyBears.class})
class GrizzlyFateTest extends BaseCardTest {

    @Test
    @DisplayName("Without threshold, Grizzly Fate creates two 2/2 green Bear tokens")
    void withoutThresholdCreatesTwoBears() {
        castFromHand(List.of());

        assertBears(2);
    }

    @Test
    @DisplayName("With threshold, Grizzly Fate creates four 2/2 green Bear tokens")
    void withThresholdCreatesFourBears() {
        castFromHand(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));

        assertBears(4);
    }

    @Test
    @DisplayName("Flashback creates Bear tokens and exiles Grizzly Fate")
    void flashbackCreatesBearsAndExilesSpell() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyFate(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertBears(4);
        harness.assertNotInGraveyard(player1, "Grizzly Fate");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Fate"));
    }

    private void castFromHand(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GrizzlyFate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void assertBears(int expectedCount) {
        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Bear"))
                .toList();
        assertThat(bears).hasSize(expectedCount);
        assertThat(bears).allSatisfy(bear -> {
            assertThat(bear.getCard().getPower()).isEqualTo(2);
            assertThat(bear.getCard().getToughness()).isEqualTo(2);
            assertThat(bear.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(bear.getCard().getSubtypes()).contains(CardSubtype.BEAR);
        });
    }
}
