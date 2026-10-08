package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrenkosCommand.class})
class KrenkosCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates two 1/1 Goblin tokens")
    void createsTwoGoblinTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new KrenkosCommand(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> tokens = findPermanents(player1, "Goblin");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> {
            assertThat(t.getEffectivePower()).isEqualTo(1);
            assertThat(t.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Resolution creates red Goblin creature tokens for the spell's controller")
    void createsGoblinTokensForSecondPlayerOnlyOnResolution() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new KrenkosCommand(), "{1}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(findPermanents(player2, "Goblin")).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(findPermanents(player2, "Goblin")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isSummoningSick()).isTrue();
        });
        harness.assertInGraveyard(player2, "Krenko's Command");
    }
}
