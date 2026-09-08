package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinotaurIllusionist.class, CrawWurm.class, Forest.class})
class MinotaurIllusionistTest extends BaseCardTest {

    @Test
    @DisplayName("The blue ability grants shroud until end of turn")
    void grantsShroudUntilEndOfTurn() {
        Permanent illusionist = harness.addToBattlefieldAndReturn(player1, new MinotaurIllusionist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, illusionist, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, illusionist, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The red ability sacrifices Minotaur Illusionist and deals its power to a creature")
    void sacrificesSelfAndDealsPowerDamage() {
        harness.addToBattlefield(player1, new MinotaurIllusionist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.assertInGraveyard(player1, "Minotaur Illusionist");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The red ability cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new MinotaurIllusionist());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
