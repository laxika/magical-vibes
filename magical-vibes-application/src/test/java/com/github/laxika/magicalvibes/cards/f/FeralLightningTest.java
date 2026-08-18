package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Feral Lightning")
class FeralLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three hasty 3/1 Elemental tokens")
    void createsThreeHastyElementals() {
        castFeralLightning();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(3);
        for (Permanent elemental : gd.playerBattlefields.get(player1.getId())) {
            if (elemental.getCard().isToken() && elemental.getCard().getName().equals("Elemental")) {
                assertThat(elemental.getEffectivePower()).isEqualTo(3);
                assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
                assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();
            }
        }
    }

    @Test
    @DisplayName("Exiles the Elemental tokens at the beginning of the next end step")
    void exilesElementalsAtNextEndStep() {
        castFeralLightning();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    private void castFeralLightning() {
        harness.setHand(player1, List.of(new FeralLightning()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
