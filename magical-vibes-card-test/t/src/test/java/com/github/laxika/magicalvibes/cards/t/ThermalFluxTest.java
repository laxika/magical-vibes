package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThermalFlux.class, SnowCoveredIsland.class, MishrasBauble.class})
class ThermalFluxTest extends BaseCardTest {

    @Test
    @DisplayName("Makes a nonsnow permanent snow until end of turn")
    void makesNonsnowPermanentSnow() {
        Permanent target = addPermanent(false);

        castThermalFlux(0, target);

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
    }

    @Test
    @DisplayName("Removes snow from a permanent until end of turn")
    void removesSnowFromPermanent() {
        Permanent target = addPermanent(true);

        castThermalFlux(1, target);

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
    }

    @Test
    @DisplayName("Rejects targets that do not match the chosen mode")
    void rejectsMismatchedTargets() {
        Permanent nonsnow = addPermanent(false);
        Permanent snow = addPermanent(true);

        assertThatThrownBy(() -> castThermalFlux(0, snow))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> castThermalFlux(1, nonsnow))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles without drawing if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addPermanent(false);
        prepareThermalFlux();
        harness.castInstant(player1, 0, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Draws a card at the next upkeep")
    void drawsAtNextUpkeep() {
        Permanent target = addPermanent(false);
        harness.setLibrary(player1, List.of(new SnowCoveredIsland()));

        castThermalFlux(0, target);

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Rechecks nonsnow targeting after another Thermal Flux resolves")
    void fizzlesWhenTargetBecomesSnowBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        harness.setHand(player1, List.of(new ThermalFlux(), new ThermalFlux()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.castInstant(player1, 0, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Rechecks snow targeting after another Thermal Flux resolves")
    void fizzlesWhenTargetStopsBeingSnowBeforeResolution() {
        Permanent target = addPermanent(true);
        harness.setHand(player1, List.of(new ThermalFlux(), new ThermalFlux()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.castInstant(player1, 0, 1, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Opposite modes apply in order and expire at end of turn")
    void oppositeModesApplyInOrder() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());

        castThermalFlux(0, target);
        castThermalFlux(1, target);

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(2);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
    }

    @Test
    @DisplayName("Delayed draw waits for a later turn and uses the stack")
    void drawWaitsForNextTurnAndTriggerResolution() {
        Permanent target = addPermanent(true);
        harness.setLibrary(player1, List.of(new SnowCoveredIsland(), new SnowCoveredIsland()));
        castThermalFlux(1, target);
        StepTriggerService triggers = GameTestEngineContext.get().getBean(StepTriggerService.class);

        harness.inMutationScope(() -> triggers.handleUpkeepTriggers(gd));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);

        gd.turnNumber++;
        harness.inMutationScope(() -> triggers.handleUpkeepTriggers(gd));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        gd.turnNumber++;
        harness.inMutationScope(() -> triggers.handleUpkeepTriggers(gd));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castThermalFlux(int mode, Permanent target) {
        prepareThermalFlux();
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }

    private void prepareThermalFlux() {
        harness.setHand(player1, List.of(new ThermalFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent addPermanent(boolean snow) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        if (!snow) {
            TestCards.mutableCard(permanent).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        }
        return permanent;
    }
}
