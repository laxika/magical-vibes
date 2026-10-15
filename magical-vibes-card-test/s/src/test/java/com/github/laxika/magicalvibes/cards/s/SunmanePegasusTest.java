package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SunmanePegasus.class})
class SunmanePegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants vigilance and lifelink until end of turn")
    void grantsVigilanceAndLifelink() {
        Permanent pegasus = addCreatureReady(player1, new SunmanePegasus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isTrue();
        assertThat(pegasus.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations grant vigilance without multiplying lifelink")
    void repeatedActivationsDoNotMultiplyLifeGain() {
        Permanent pegasus = addCreatureReady(player1, new SunmanePegasus());
        Permanent other = addCreatureReady(player1, new SunmanePegasus());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(pegasus.isTapped()).isFalse();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A tapped summoning-sick Pegasus can activate without untapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent pegasus = addCreatureReady(player1, new SunmanePegasus());
        pegasus.setSummoningSick(true);
        pegasus.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.LIFELINK)).isTrue();
        assertThat(pegasus.isTapped()).isTrue();
    }
}
