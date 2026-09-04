package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KjeldoranEliteGuard.class, BalduvianBears.class, Mountain.class})
class KjeldoranEliteGuardTest extends BaseCardTest {

    private void enterCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(TurnStep.BEGINNING_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), EnumSet.of(TurnStep.BEGINNING_OF_COMBAT));
    }

    @Test
    @DisplayName("Cannot activate outside combat")
    void cannotActivateOutsideCombat() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate after combat")
    void cannotActivateAfterCombat() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gives target creature +2/+2 until end of turn during combat")
    void pumpsTargetDuringCombat() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 2);
        assertThat(guard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The pump expires at end of turn")
    void pumpExpiresAtEndOfTurn() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Can target an opponent creature")
    void canTargetOpponentsCreature() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kjeldoran Elite Guard");
        harness.assertInGraveyard(player1, "Kjeldoran Elite Guard");
        harness.assertInHand(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Sacrifices itself when the pumped creature leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kjeldoran Elite Guard");
        harness.assertInGraveyard(player1, "Kjeldoran Elite Guard");
        harness.assertInHand(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Does not sacrifice when the pumped creature stays on the battlefield")
    void doesNotSacrificeIfTargetStays() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kjeldoran Elite Guard");
        harness.assertOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Delayed sacrifice expires at end of turn")
    void delayedSacrificeExpiresAtEndOfTurn() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterCombat();
        harness.activateAbility(player1, indexOf(guard), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kjeldoran Elite Guard");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent guard = addCreatureReady(player1, new KjeldoranEliteGuard());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        enterCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(guard), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
