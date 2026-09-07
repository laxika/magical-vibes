package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Thatcher Revolt")
class ThatcherRevoltTest extends BaseCardTest {

    private void castRevolt() {
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creates three 1/1 Human tokens with haste")
    void createsThreeHastyHumans() {
        castRevolt();

        List<Permanent> humans = findPermanents(player1, "Human");

        assertThat(humans).hasSize(3);
        assertThat(humans).allSatisfy(human -> {
            assertThat(human.getCard().getPower()).isEqualTo(1);
            assertThat(human.getCard().getToughness()).isEqualTo(1);
            assertThat(human.getCard().getKeywords()).contains(Keyword.HASTE);
        });
    }

    @Test
    @DisplayName("The tokens are sacrificed at the beginning of the next end step")
    void tokensSacrificedAtNextEndStep() {
        castRevolt();
        harness.assertOnBattlefield(player1, "Human");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Human");
    }
}
