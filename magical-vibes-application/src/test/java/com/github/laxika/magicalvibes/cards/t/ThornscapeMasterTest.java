package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThornscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability deals 2 damage to a target creature")
    void dealsTwoDamageToTargetCreature() {
        Permanent source = addCreatureReady(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new CentaurCourser());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability grants chosen-color protection to a target creature until end of turn")
    void grantsChosenProtectionUntilEndOfTurn() {
        Permanent source = addCreatureReady(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Neither ability can target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent source = addCreatureReady(player1, new ThornscapeMaster());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                0,
                null,
                forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                1,
                null,
                forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
