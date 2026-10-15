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

@CardUsed({Warning.class, BalduvianBears.class, ZuranSpellcaster.class})
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
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

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

    @Test
    @DisplayName("Does not prevent damage from another attacking creature")
    void otherAttackerStillDealsDamage() {
        Permanent warned = addAttacker(player1, player2, 2, 2);
        addAttacker(player1, player2, 2, 2);

        castWarning(warned);
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt to the warned creature")
    void warnedAttackerStillReceivesCombatDamage() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        castWarning(attacker);
        resolveCombat();

        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertOnBattlefield(player2, "Balduvian Bears");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can target an opponent's attacking creature")
    void canTargetOpponentsAttacker() {
        harness.forceActivePlayer(player2);
        Permanent attacker = addAttacker(player2, player1, 2, 2);

        castWarning(attacker);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    private void castWarning(Permanent target) {
        harness.setHand(player1, List.of(new Warning()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
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
