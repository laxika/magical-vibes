package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CavePeopleTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Cave People +1/-2 until end of turn")
    void attackingBoostsSelf() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(1);
        assertThat(cavePeople.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The +1/-2 wears off at end of turn")
    void boostWearsOff() {
        Permanent cavePeople = addCreatureReady(player1, new CavePeople());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cavePeople.getPowerModifier()).isEqualTo(0);
        assertThat(cavePeople.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{1}{R}{R}, {T} ability grants mountainwalk to the target creature")
    void grantsMountainwalkToTarget() {
        addCreatureReady(player1, new CavePeople());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk wears off at end of turn")
    void mountainwalkWearsOff() {
        addCreatureReady(player1, new CavePeople());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Ability targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new CavePeople());
        Permanent forest = addCreatureReady(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
