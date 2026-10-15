package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonFodder.class})
class DragonFodderTest extends BaseCardTest {

    private List<Permanent> goblins() {
        return findPermanents(player1, "Goblin");
    }

    @Test
    @DisplayName("Cast creates two 1/1 Goblin tokens")
    void createsTwoGoblinTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        List<Permanent> tokens = goblins();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> {
            assertThat(t.getEffectivePower()).isEqualTo(1);
            assertThat(t.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Goblins are red creature tokens that enter untapped and summoning sick")
    void createsRedGoblinCreatureTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DragonFodder(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(goblins()).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Fodder");
    }

    @Test
    @DisplayName("Tokens are created only on resolution and belong to the spell's controller")
    void createsTokensForSecondPlayerOnlyOnResolution() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new DragonFodder(), "{1}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(findPermanents(player2, "Goblin")).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(findPermanents(player2, "Goblin")).hasSize(2);
        harness.assertInGraveyard(player2, "Dragon Fodder");
    }
}
