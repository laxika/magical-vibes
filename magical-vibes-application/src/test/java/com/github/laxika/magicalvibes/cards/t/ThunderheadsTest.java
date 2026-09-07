package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thunderheads.class})
class ThunderheadsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 3/3 blue Weird token with defender and flying")
    void createsWeirdToken() {
        castThunderheads(List.of());

        Permanent token = getTokens().getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WEIRD);
        assertThat(gqs.hasKeyword(gd, token, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Replicate creates one token for each replicate payment")
    void replicateCreatesTokensForEachPayment() {
        castThunderheads(List.of("{2}{U}", "{2}{U}"));

        assertThat(getTokens()).hasSize(3);
    }

    @Test
    @DisplayName("Exiles the token at the next end step")
    void exilesTokenAtNextEndStep() {
        castThunderheads(List.of());
        assertThat(getTokens()).hasSize(1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(getTokens()).isEmpty();
    }

    private void castThunderheads(List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Thunderheads()));
        harness.addMana(player1, ManaColor.BLUE, 1 + replicatePayments.size());
        harness.addMana(player1, ManaColor.COLORLESS, 2 + replicatePayments.size() * 2);
        harness.castInstantWithRepeatedCosts(player1, 0, null, replicatePayments);
        resolveAllTriggers();
    }

    private List<Permanent> getTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
