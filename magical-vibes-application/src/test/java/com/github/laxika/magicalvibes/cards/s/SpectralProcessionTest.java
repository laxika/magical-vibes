package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SpectralProcession.class)
class SpectralProcessionTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Resolving Spectral Procession creates three Spirit tokens")
    void resolvingCreatesThreeTokens() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new SpectralProcession()));
        harness.addMana(player1, ManaColor.WHITE, 3); // {2/W}{2/W}{2/W} paid with three white

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = spiritTokens(player1);
        assertThat(tokens).hasSize(3);
    }

    @Test
    @DisplayName("Created tokens are 1/1 white Spirits with flying")
    void tokensHaveCorrectStats() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new SpectralProcession()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = spiritTokens(player1);

        assertThat(tokens).hasSize(3);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Created tokens are white Spirits")
    void tokensAreWhiteSpirits() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new SpectralProcession()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(spiritTokens(player1)).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        });
    }

    @Test
    @DisplayName("Spectral Procession goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new SpectralProcession()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spectral Procession");
    }

    private List<Permanent> spiritTokens(Player player) {
        return findPermanents(player, "Spirit").stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }
}
