package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinOutlander;
import com.github.laxika.magicalvibes.cards.t.TukatongueThallid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuicidalCharge.class, GoblinOutlander.class, TukatongueThallid.class})
class SuicidalChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing gives opponents' creatures -1/-1 and forces them to attack this turn")
    void weakensAndForcesOpponentCreatures() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent enemyBear = addCreatureReady(player2, new GoblinOutlander());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(1);
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
        // The enchantment was sacrificed as the cost.
        harness.assertInGraveyard(player1, "Suicidal Charge");
    }

    @Test
    @DisplayName("Leaves the controller's own creatures untouched")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent ownBear = addCreatureReady(player1, new GoblinOutlander());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(ownBear.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The -1/-1 and must-attack wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent enemyBear = addCreatureReady(player2, new GoblinOutlander());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
        assertThat(enemyBear.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void sacrificeIsPaidBeforeTheAbilityResolves() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent enemy = addCreatureReady(player2, new GoblinOutlander());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Suicidal Charge");
        harness.assertInGraveyard(player1, "Suicidal Charge");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(2);
        assertThat(enemy.isMustAttackThisTurn()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(1);
        assertThat(enemy.isMustAttackThisTurn()).isTrue();
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotLaterArrivals() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = addCreatureReady(player2, new GoblinOutlander());

        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player2, new GoblinOutlander());

        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(1);
        assertThat(beforeResolution.isMustAttackThisTurn()).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(afterResolution.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void killsOneToughnessCreatureButDoesNotAffectItsDeathTriggerToken() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        harness.addToBattlefield(player2, new TukatongueThallid());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Tukatongue Thallid");
        resolveAllTriggers();

        Permanent token = findPermanent(player2, "Saproling");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void ableCreatureCannotBeOmittedFromAttackDeclaration() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        addCreatureReady(player2, new GoblinOutlander());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThatCode(() -> declareAttackers(player2, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    void tappedAndSummoningSickCreaturesAreNotRequiredToAttack() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent tapped = addCreatureReady(player2, new GoblinOutlander());
        tapped.tap();
        harness.addToBattlefield(player2, new GoblinOutlander());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatCode(() -> declareAttackers(player2, List.of())).doesNotThrowAnyException();
    }
}
