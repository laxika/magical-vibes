package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Waylay")
class WaylayTest extends BaseCardTest {

    private void castWaylay() {
        harness.setHand(player1, List.of(new Waylay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creates three 2/2 white Knight tokens")
    void createsKnightTokens() {
        castWaylay();

        List<Permanent> knights = findPermanents(player1, "Knight");
        assertThat(knights).hasSize(3);
        assertThat(knights).allSatisfy(knight -> {
            assertThat(knight.getCard().getPower()).isEqualTo(2);
            assertThat(knight.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Exiles the tokens at the beginning of the next cleanup step")
    void exilesTokensAtNextCleanup() {
        castWaylay();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Knight")).hasSize(3);

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()).stream()
                .filter(Card::isToken)
                .count()).isEqualTo(3);
    }
}
