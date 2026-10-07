package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SukiKyoshiCaptain.class, GorillaWarrior.class, GrizzlyBears.class})
class SukiKyoshiCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Other Warriors you control get +1/+1")
    void buffsOtherWarriorsYouControl() {
        harness.addToBattlefield(player1, new SukiKyoshiCaptain());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentWarrior = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonWarrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonWarrior)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentWarrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentWarrior)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability gives double strike to attacking Warriors you control")
    void grantsDoubleStrikeToAttackingWarriors() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiCaptain());
        Permanent attackingWarrior = addCreatureReady(player1, new GorillaWarrior());
        Permanent nonAttackingWarrior = addCreatureReady(player1, new GorillaWarrior());
        Permanent attackingNonWarrior = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addCreatureReady(player2, new GorillaWarrior());
        suki.setAttacking(true);
        attackingWarrior.setAttacking(true);
        attackingNonWarrior.setAttacking(true);
        opponentAttacker.setAttacking(true);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, suki, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attackingWarrior, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttackingWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attackingNonWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentAttacker, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        gd.interaction.clearAwaitingInput();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Suki does not boost her own combat damage")
    void doesNotBoostHerself() {
        addCreatureReady(player1, new SukiKyoshiCaptain());
        int defendingLife = gd.playerLifeTotals.get(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, defendingLife - 3);
    }

    @Test
    @DisplayName("Activating before combat does not grant double strike to later attackers")
    void doesNotGrantDoubleStrikeToLaterAttackers() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiCaptain());
        Permanent warrior = addCreatureReady(player1, new GorillaWarrior());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        suki.setAttacking(true);
        warrior.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, suki, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability selects attacking Warriors at resolution")
    void selectsWarriorsAtResolution() {
        addCreatureReady(player1, new SukiKyoshiCaptain());
        Permanent removedAttacker = addCreatureReady(player1, new GorillaWarrior());
        Permanent newAttacker = addCreatureReady(player1, new GorillaWarrior());
        removedAttacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        removedAttacker.setAttacking(false);
        newAttacker.setAttacking(true);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, removedAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, newAttacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability resolves without Suki and the grant persists after combat")
    void grantDoesNotDependOnSourceOrContinuedAttacking() {
        Permanent suki = addCreatureReady(player1, new SukiKyoshiCaptain());
        Permanent warrior = addCreatureReady(player1, new GorillaWarrior());
        warrior.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(suki);
        gd.playerGraveyards.get(player1.getId()).add(suki.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DOUBLE_STRIKE)).isTrue();
        warrior.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
    }

}
