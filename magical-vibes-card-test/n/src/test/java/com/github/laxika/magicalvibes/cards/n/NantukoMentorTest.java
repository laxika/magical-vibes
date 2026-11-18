package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.Afflict;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({NantukoMentor.class, NantukoDisciple.class, Forest.class, Afflict.class, AngelicWall.class})
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
        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(4);
    }

    @Test
    @DisplayName("Uses the target's power when the ability resolves")
    void usesTargetPowerAtResolution() {
        Permanent mentor = addCreatureReady(player1, new NantukoMentor());
        Permanent disciple = addCreatureReady(player1, new NantukoDisciple());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, mentor.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, mentor.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(6);
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
        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, disciple)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, disciple)).isEqualTo(2);
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

    @Test
    void usesPowerChangedInResponseAndKeepsTheResolvedBoostFixed() {
        Permanent mentor = addCreatureReady(player1, new NantukoMentor());
        addCreatureReady(player1, new NantukoDisciple());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, mentor.getId());
        harness.activateAbility(player1, 1, null, mentor.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(6);

        harness.setHand(player1, List.of(new Afflict()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, mentor.getId());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(5);
    }

    @Test
    void negativeTargetPowerGivesNoBoost() {
        addCreatureReady(player1, new NantukoMentor());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        harness.setHand(player1, List.of(new Afflict()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, wall.getId());
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new NantukoMentor());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mentor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        Permanent mentor = addCreatureReady(player1, new NantukoMentor());
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, null, mentor.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mentor.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(2);
    }
}
