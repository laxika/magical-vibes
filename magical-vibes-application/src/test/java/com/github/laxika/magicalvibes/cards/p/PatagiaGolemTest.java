package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed(PatagiaGolem.class)
class PatagiaGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants flying until end of turn")
    void resolvingGrantsFlying() {
        Permanent golem = addCreatureReady(player1, new PatagiaGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can activate without tapping the summoning-sick creature")
    void canActivateWithoutTappingOrHaste() {
        Permanent golem = addCreatureReady(player1, new PatagiaGolem());
        golem.tap();
        golem.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn cleanup")
    void flyingResetsAtEndOfTurn() {
        Permanent golem = addCreatureReady(player1, new PatagiaGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new PatagiaGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
