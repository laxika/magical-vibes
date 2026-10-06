package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RixMaadiGuildmage.class})
class RixMaadiGuildmageTest extends BaseCardTest {

    private void readyGuildmage() {
        addCreatureReady(player1, new RixMaadiGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("{B}{R}: target blocking creature gets -1/-1 until end of turn")
    void shrinksBlockingCreature() {
        readyGuildmage();
        Permanent blocker = addCreatureReady(player2, new RixMaadiGuildmage());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking shrink wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        readyGuildmage();
        Permanent blocker = addCreatureReady(player2, new RixMaadiGuildmage());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-blocking creature is an illegal target")
    void rejectsNonBlockingCreature() {
        readyGuildmage();
        Permanent idle = addCreatureReady(player2, new RixMaadiGuildmage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{B}{R}: target player who lost life this turn loses 1 life")
    void drainsPlayerWhoLostLife() {
        readyGuildmage();
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Can target self if you lost life this turn")
    void canTargetSelfWhoLostLife() {
        readyGuildmage();
        gd.lifeLostThisTurn.put(player1.getId(), 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Player who has not lost life this turn is an illegal target")
    void rejectsPlayerWhoHasNotLostLife() {
        readyGuildmage();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shrink does not resolve if the target stops blocking")
    void rechecksBlockingOnResolution() {
        readyGuildmage();
        Permanent blocker = addCreatureReady(player2, new RixMaadiGuildmage());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        blocker.setBlocking(false);
        blocker.getBlockingTargets().clear();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Repeated shrink activations can kill your own blocking creature")
    void repeatedShrinkCanKillOwnBlocker() {
        readyGuildmage();
        Permanent blocker = addCreatureReady(player1, new RixMaadiGuildmage());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Rix Maadi Guildmage");
    }

    @Test
    @DisplayName("Tapped summoning-sick guildmage can target a player who regained lost life")
    void lifeGainDoesNotUndoLifeLossEligibility() {
        readyGuildmage();
        Permanent guildmage = gd.playerBattlefields.get(player1.getId()).getFirst();
        guildmage.tap();
        guildmage.setSummoningSick(true);
        harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 2, "test life loss");
        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
        harness.assertLife(player1, controllerLifeBefore);
    }
}
