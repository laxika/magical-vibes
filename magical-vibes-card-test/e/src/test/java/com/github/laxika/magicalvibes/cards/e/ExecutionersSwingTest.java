package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BorosReckoner;
import com.github.laxika.magicalvibes.cards.g.GiantAdephage;
import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.cards.m.Mugging;
import com.github.laxika.magicalvibes.cards.r.RuinationWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExecutionersSwing.class, GreensideWatcher.class, GiantAdephage.class,
        BorosReckoner.class, Mugging.class, RuinationWurm.class})
class ExecutionersSwingTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a creature that dealt combat damage this turn")
    void killsCreatureThatDealtCombatDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        markDealtDamage(bears);

        castSwing(bears);

        harness.assertNotOnBattlefield(player2, "Greenside Watcher");
        harness.assertInGraveyard(player2, "Greenside Watcher");
    }

    @Test
    @DisplayName("A surviving creature gets -5/-5 that wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new GiantAdephage());
        markDealtDamage(avatar);

        castSwing(avatar);

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(7);
    }

    @Test
    @DisplayName("Cannot target a creature that dealt no damage this turn")
    void cannotTargetCreatureThatDealtNoDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());

        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that was only dealt damage itself")
    void cannotTargetCreatureThatOnlyTookDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void actualCombatDamageToPlayerMakesCreatureEligible() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        watcher.setAttacking(true);
        watcher.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player1, 18);

        castSwing(watcher);

        harness.assertInGraveyard(player2, "Greenside Watcher");
    }

    @Test
    void combatDamageToCreatureMakesOwnCreatureEligible() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new RuinationWurm());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        wurm.setAttacking(true);
        wurm.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Ruination Wurm");
        harness.assertInGraveyard(player2, "Greenside Watcher");

        castSwing(wurm);

        harness.assertInGraveyard(player1, "Ruination Wurm");
    }

    @Test
    void noncombatDamageToPlayerMakesCreatureEligible() {
        Permanent reckoner = harness.addToBattlefieldAndReturn(player2, new BorosReckoner());
        harness.setHand(player1, List.of(new Mugging()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, reckoner.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);

        castSwing(reckoner);

        harness.assertInGraveyard(player2, "Boros Reckoner");
    }

    @Test
    void noncombatDamageToCreatureMakesCreatureEligible() {
        Permanent reckoner = harness.addToBattlefieldAndReturn(player2, new BorosReckoner());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GiantAdephage());
        harness.setHand(player1, List.of(new Mugging()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, reckoner.getId());
        harness.handlePermanentChosen(player2, victim.getId());
        harness.passBothPriorities();
        assertThat(victim.getMarkedDamage()).isEqualTo(2);

        castSwing(reckoner);

        harness.assertInGraveyard(player2, "Boros Reckoner");
    }

    @Test
    void preventedCombatDamageDoesNotMakeCreatureEligible() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        watcher.setAttacking(true);
        watcher.setSummoningSick(false);
        gd.preventAllCombatDamage = true;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertLife(player1, 20);
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, watcher.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageFromPreviousTurnDoesNotMakeCreatureEligible() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player2, new GreensideWatcher());
        markDealtDamage(watcher);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, watcher.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void markDealtDamage(Permanent permanent) {
        gd.recordDamageDealtBySource(permanent.getId(), 2);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ExecutionersSwing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void castSwing(Permanent target) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
