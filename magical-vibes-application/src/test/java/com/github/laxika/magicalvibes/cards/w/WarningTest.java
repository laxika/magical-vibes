package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Warning.class, BalduvianBears.class})
class WarningTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage the target creature would deal to a player")
    void preventsCombatDamageDealtByCreature() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castWarning(attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents combat damage the target creature would deal to a blocker")
    void preventsCombatDamageDealtToBlocker() {
        Permanent attacker = addAttacker(player1, player2, 2, 5);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        castWarning(attacker);
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed(ZuranSpellcaster.class)
    @DisplayName("Does not prevent noncombat damage dealt by the target creature")
    void doesNotPreventNoncombatDamage() {
        Permanent attacker = addCreatureReady(player1, new ZuranSpellcaster());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        castWarning(attacker);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        harness.activateAbility(player1, attackerIndex, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castWarning(attacker);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // POSTCOMBAT_MAIN -> END_STEP

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent bystander = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new Warning()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttackingBeforeResolution() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castWarningWithoutResolving(attacker);
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles (illegal target)")).isTrue();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    private void castWarning(Permanent target) {
        castWarningWithoutResolving(target);
        harness.passBothPriorities();
    }

    private void castWarningWithoutResolving(Permanent target) {
        harness.setHand(player1, List.of(new Warning()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        BalduvianBears bears = new BalduvianBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = addCreatureReady(owner, bears);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
