package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NantukoMentor.class, NantukoDisciple.class, Forest.class})
class NantukoMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +X/+X where X is its power")
    void boostsTargetByItsPower() {
        Permanent mentor = addCreatureReady(player1, new NantukoMentor());
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new NantukoDisciple());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, disciple.getId());
        harness.passBothPriorities();

        assertThat(mentor.isTapped()).isTrue();
        assertThat(disciple.getEffectivePower()).isEqualTo(4);
        assertThat(disciple.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Uses the target's power when the ability resolves")
    void usesTargetPowerAtResolution() {
        Permanent mentor = addCreatureReady(player1, new NantukoMentor());
        Permanent disciple = addCreatureReady(player1, new NantukoDisciple());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, mentor.getId());
        harness.passBothPriorities();
        assertThat(mentor.getEffectivePower()).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, mentor.getId());
        harness.passBothPriorities();

        assertThat(mentor.getEffectivePower()).isEqualTo(6);
        assertThat(mentor.getEffectiveToughness()).isEqualTo(6);
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new NantukoMentor());
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new NantukoDisciple());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, disciple.getId());
        harness.passBothPriorities();
        assertThat(disciple.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(disciple.getEffectivePower()).isEqualTo(2);
        assertThat(disciple.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new NantukoMentor());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
