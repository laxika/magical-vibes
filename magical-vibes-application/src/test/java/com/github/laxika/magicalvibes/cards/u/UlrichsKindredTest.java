package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({UlrichsKindred.class, GreaterWerewolf.class, GrizzlyBears.class})
class UlrichsKindredTest extends BaseCardTest {

    private Permanent addKindred() {
        return addCreatureReady(player1, new UlrichsKindred());
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Grants indestructible to an attacking Wolf, then it wears off at end of turn")
    void grantsIndestructibleToAttackingWolf() {
        addKindred();
        Permanent attackingWolf = addCreatureReady(player1, new UlrichsKindred());
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
        Permanent attackingWerewolf = addCreatureReady(player1, new GreaterWerewolf());
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
        Permanent idleWolf = addCreatureReady(player1, new UlrichsKindred());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, idleWolf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking non-Wolf/Werewolf creature")
    void cannotTargetAttackingNonWolf() {
        addKindred();
        Permanent attackingBear = addCreatureReady(player1, new GrizzlyBears());
        attackingBear.setAttacking(true);
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, attackingBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfWhileAttacking() {
        Permanent kindred = addKindred();
        kindred.setAttacking(true);
        kindred.tap();
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, kindred.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kindred, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void canProtectOpponentsAttackingWolf() {
        addKindred();
        Permanent wolf = addCreatureReady(player2, new UlrichsKindred());
        wolf.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void doesNotGrantIndestructibleIfTargetStopsAttackingBeforeResolution() {
        addKindred();
        Permanent wolf = addCreatureReady(player1, new UlrichsKindred());
        wolf.setAttacking(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, wolf.getId());
        wolf.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent kindred = addKindred();
        Permanent wolf = addCreatureReady(player1, new UlrichsKindred());
        wolf.setAttacking(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, wolf.getId());
        gd.playerBattlefields.get(player1.getId()).remove(kindred);
        gd.playerGraveyards.get(player1.getId()).add(kindred.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
