package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UlrichsKindredTest extends BaseCardTest {

    private Permanent addKindred() {
        Permanent kindred = harness.addToBattlefieldAndReturn(player1, new UlrichsKindred());
        kindred.setSummoningSick(false);
        return kindred;
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Grants indestructible to an attacking Wolf, then it wears off at end of turn")
    void grantsIndestructibleToAttackingWolf() {
        addKindred();
        Permanent attackingWolf = harness.addToBattlefieldAndReturn(player1, new UlrichsKindred());
        attackingWolf.setSummoningSick(false);
        attackingWolf.setAttacking(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, attackingWolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingWolf, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingWolf, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Grants indestructible to an attacking Werewolf")
    void grantsIndestructibleToAttackingWerewolf() {
        addKindred();
        Permanent attackingWerewolf = harness.addToBattlefieldAndReturn(player1, new GreaterWerewolf());
        attackingWerewolf.setSummoningSick(false);
        attackingWerewolf.setAttacking(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, attackingWerewolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingWerewolf, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a Wolf that is not attacking")
    void cannotTargetNonAttackingWolf() {
        addKindred();
        Permanent idleWolf = harness.addToBattlefieldAndReturn(player1, new UlrichsKindred());
        idleWolf.setSummoningSick(false);
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, idleWolf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking non-Wolf/Werewolf creature")
    void cannotTargetAttackingNonWolf() {
        addKindred();
        Permanent attackingBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attackingBear.setSummoningSick(false);
        attackingBear.setAttacking(true);
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, attackingBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
