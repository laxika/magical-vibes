package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChariotOfTheSun.class, GoblinEliteInfantry.class})
class ChariotOfTheSunTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying and sets base toughness to 1 without touching base power")
    void grantsFlyingAndSetsBaseToughness() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters apply on top of the new base toughness")
    void countersApplyOnTopOfNewBaseToughness() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        // Base 2/1 after the ability, counters still apply on top: 4/3
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flying and the base toughness set both wear off at cleanup")
    void wearsOffAtCleanup() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player2, new GoblinEliteInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID opponentGoblinId = harness.getPermanentId(player2, "Goblin Elite Infantry");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentGoblinId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Requires two generic mana to activate")
    void requiresTwoGenericMana() {
        harness.addToBattlefield(player1, new ChariotOfTheSun());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        Permanent chariot = harness.addToBattlefieldAndReturn(player1, new ChariotOfTheSun());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinEliteInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, goblin.getId());

        assertThat(chariot.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent chariot = harness.addToBattlefieldAndReturn(player1, new ChariotOfTheSun());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, chariot.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
