package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrandOfDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates a single 1/1 Knight token with banding")
    void createsKnightTokenWithBanding() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ErrandOfDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> knights = findPermanents(player1, "Knight");
        assertThat(knights).hasSize(1);
        Permanent knight = knights.getFirst();
        assertThat(knight.getEffectivePower()).isEqualTo(1);
        assertThat(knight.getEffectiveToughness()).isEqualTo(1);
        assertThat(knight.hasKeyword(Keyword.BANDING)).isTrue();
    }
}
