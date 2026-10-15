package com.github.laxika.magicalvibes.cards.n;

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

@CardUsed(NoblePanther.class)
class NoblePantherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent panther = addCreatureReady(player1, new NoblePanther());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, panther, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent panther = addCreatureReady(player1, new NoblePanther());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, panther, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability requires one generic mana")
    void requiresGenericMana() {
        addCreatureReady(player1, new NoblePanther());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while tapped and does not tap the panther")
    void canActivateWhileTapped() {
        Permanent panther = addCreatureReady(player1, new NoblePanther());
        panther.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(panther.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick panther can pay colored mana to gain first strike")
    void canActivateWhileSummoningSick() {
        Permanent panther = harness.addToBattlefieldAndReturn(player1, new NoblePanther());
        panther.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, panther, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(panther.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, panther, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(panther.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Repeated activations grant first strike only to the source panther")
    void repeatedActivationsAffectOnlySource() {
        Permanent panther = addCreatureReady(player1, new NoblePanther());
        Permanent otherPanther = addCreatureReady(player1, new NoblePanther());
        Permanent opposingPanther = addCreatureReady(player2, new NoblePanther());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, panther, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherPanther, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingPanther, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(panther.isTapped()).isFalse();
    }
}
