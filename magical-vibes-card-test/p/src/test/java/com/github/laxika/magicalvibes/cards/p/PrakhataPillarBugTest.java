package com.github.laxika.magicalvibes.cards.p;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrakhataPillarBug.class})
class PrakhataPillarBugTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants lifelink until end of turn")
    void grantsLifelinkUntilEndOfTurn() {
        Permanent bug = addCreatureReady(player1, new PrakhataPillarBug());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bug, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bug, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Ability requires black mana")
    void requiresBlackMana() {
        addCreatureReady(player1, new PrakhataPillarBug());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Repeated activations gain life only once for combat damage")
    void repeatedActivationsDoNotMultiplyLifeGain() {
        addCreatureReady(player1, new PrakhataPillarBug());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A tapped summoning-sick Pillar-Bug can activate without granting lifelink to others")
    void tappedSummoningSickSourceCanActivateOnlyForItself() {
        Permanent bug = addCreatureReady(player1, new PrakhataPillarBug());
        bug.setSummoningSick(true);
        bug.tap();
        Permanent otherBug = addCreatureReady(player1, new PrakhataPillarBug());
        Permanent opposingBug = addCreatureReady(player2, new PrakhataPillarBug());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasKeyword(gd, bug, Keyword.LIFELINK)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bug, Keyword.LIFELINK)).isTrue();
        assertThat(bug.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, otherBug, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingBug, Keyword.LIFELINK)).isFalse();
    }
}
