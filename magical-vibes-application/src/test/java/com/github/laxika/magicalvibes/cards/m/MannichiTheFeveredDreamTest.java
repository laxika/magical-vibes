package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MannichiTheFeveredDreamTest extends BaseCardTest {

    @Test
    @DisplayName("Switches the power and toughness of each creature")
    void switchesEachCreature() {
        Permanent mannichi = harness.addToBattlefieldAndReturn(player1, new MannichiTheFeveredDream());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mannichi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mannichi)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Switch wears off at end of turn")
    void switchWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new MannichiTheFeveredDream());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }
}
