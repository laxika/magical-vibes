package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Goblin Ski Patrol")
class GoblinSkiPatrolTest extends BaseCardTest {

    private Permanent addPatrol() {
        Permanent patrol = new Permanent(new GoblinSkiPatrol());
        patrol.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(patrol);
        return patrol;
    }

    private void addSnowMountain() {
        Permanent mountain = new Permanent(new Mountain());
        TestCards.mutableCard(mountain).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player1.getId()).add(mountain);
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Activating gives +2/+0 and flying")
    void activationBoostsAndGrantsFlying() {
        Permanent patrol = addPatrol();
        addSnowMountain();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patrol)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patrol)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("It is sacrificed at the beginning of the next end step")
    void sacrificedAtNextEndStep() {
        addPatrol();
        addSnowMountain();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goblin Ski Patrol");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Ski Patrol");
        harness.assertInGraveyard(player1, "Goblin Ski Patrol");
    }

    @Test
    @DisplayName("Cannot activate without a snow Mountain")
    void cannotActivateWithoutSnowMountain() {
        addPatrol();
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Mountain()));
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate a second time")
    void cannotActivateTwice() {
        addPatrol();
        addSnowMountain();
        payMana();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Still cannot activate again on a later turn")
    void cannotActivateAgainNextTurn() {
        addPatrol();
        addSnowMountain();
        payMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        gd.activatedAbilityUsesThisTurn.clear();
        payMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
