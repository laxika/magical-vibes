package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurnoutBashtronaut.class})
class BurnoutBashtronautTest extends BaseCardTest {

    @Test
    void gainsDoubleStrikeAtMaxSpeed() {
        Permanent bashtronaut = addCreatureReady(player1, new BurnoutBashtronaut());

        assertThat(gqs.hasKeyword(gd, bashtronaut, Keyword.DOUBLE_STRIKE)).isFalse();

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.hasKeyword(gd, bashtronaut, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void activatedAbilityBoostsPowerUntilEndOfTurn() {
        Permanent bashtronaut = addCreatureReady(player1, new BurnoutBashtronaut());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bashtronaut)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bashtronaut)).isEqualTo(1);
    }

    @Test
    void enteringStartsSpeedWithoutResettingExistingSpeed() {
        harness.castFromHand(player1, new BurnoutBashtronaut(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();

        gd.playerSpeeds.put(player1.getId(), 3);
        harness.enterBattlefieldAndReturn(player1, new BurnoutBashtronaut());
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void doubleStrikeUsesOnlyControllersSpeedAndDisappearsBelowMaxSpeed() {
        Permanent bashtronaut = addCreatureReady(player1, new BurnoutBashtronaut());
        Permanent opposingBashtronaut = addCreatureReady(player2, new BurnoutBashtronaut());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);

        assertThat(gqs.hasKeyword(gd, bashtronaut, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingBashtronaut, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerSpeeds.put(player1.getId(), 4);
        assertThat(gqs.hasKeyword(gd, bashtronaut, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerSpeeds.put(player1.getId(), 3);
        assertThat(gqs.hasKeyword(gd, bashtronaut, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void repeatedActivationsStackAndOnlyBoostTheirSource() {
        Permanent bashtronaut = addCreatureReady(player1, new BurnoutBashtronaut());
        Permanent other = addCreatureReady(player1, new BurnoutBashtronaut());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bashtronaut)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bashtronaut)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new BurnoutBashtronaut());
        addCreatureReady(player2, new BurnoutBashtronaut());
        addCreatureReady(player2, new BurnoutBashtronaut());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void maxSpeedDealsBothCombatDamageStepsWhenUnblocked() {
        addCreatureReady(player1, new BurnoutBashtronaut());
        gd.playerSpeeds.put(player1.getId(), 4);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void multipleAttackersIncreaseSpeedOnlyOnceInTheTurn() {
        addCreatureReady(player1, new BurnoutBashtronaut());
        addCreatureReady(player1, new BurnoutBashtronaut());
        harness.runStateBasedActions();

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void reachingMaxSpeedAfterNormalCombatDamageDoesNotDealExtraDamage() {
        Permanent bashtronaut = addCreatureReady(player1, new BurnoutBashtronaut());
        gd.playerSpeeds.put(player1.getId(), 3);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bashtronaut, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void canActivateWhileTappedOnOpponentsTurn() {
        Permanent bashtronaut = addCreatureReady(player1, new BurnoutBashtronaut());
        bashtronaut.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bashtronaut)).isEqualTo(2);
        assertThat(bashtronaut.isTapped()).isTrue();
    }
}
