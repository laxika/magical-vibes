package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HeronOfHope.class)
class HeronOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains one additional life")
    void controllerGainsAdditionalLife() {
        harness.addToBattlefield(player1, new HeronOfHope());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Additional life gain does not affect an opponent")
    void opponentDoesNotGainAdditionalLife() {
        harness.addToBattlefield(player1, new HeronOfHope());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Ability grants lifelink until end of turn")
    void abilityGrantsLifelinkUntilEndOfTurn() {
        Permanent heron = harness.addToBattlefieldAndReturn(player1, new HeronOfHope());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, heron, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, heron, Keyword.LIFELINK)).isFalse();
    }
}
