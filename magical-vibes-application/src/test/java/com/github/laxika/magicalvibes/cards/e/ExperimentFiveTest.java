package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.ManaConfluence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExperimentFiveTest extends BaseCardTest {

    @Test
    @DisplayName("{Z} can be paid with mana from a multicolored source")
    void multicoloredSourceManaPaysZCost() {
        Permanent confluence = harness.addToBattlefieldAndReturn(player1, new ManaConfluence());
        Permanent experiment = addCreatureReady(player1, new ExperimentFive());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(confluence.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, experiment)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, experiment)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("{Z} cannot be paid with ordinary mana")
    void ordinaryManaCannotPayZCost() {
        addCreatureReady(player1, new ExperimentFive());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
