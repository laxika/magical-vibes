package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BurnoutBashtronautTest extends BaseCardTest {

    @Test
    void gainsDoubleStrikeAtMaxSpeed() {
        Permanent bashtronaut = addCreatureReady(player1, new BurnoutBashtronaut());

        assertThat(gqs.hasKeyword(gd, bashtronaut, Keyword.DOUBLE_STRIKE)).isFalse();

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.hasKeyword(gd, bashtronaut, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void activatedAbilityBoostsPowerUntilEndOfTurn() {
        Permanent bashtronaut = addCreatureReady(player1, new BurnoutBashtronaut());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bashtronaut)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bashtronaut)).isEqualTo(1);
    }
}
