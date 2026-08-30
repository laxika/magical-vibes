package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThermalFluxTest extends BaseCardTest {

    @Test
    @DisplayName("Makes a nonsnow permanent snow until end of turn")
    void makesNonsnowPermanentSnow() {
        Permanent target = addPermanent(false);

        castThermalFlux(0, target);

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        new TurnCleanupService(null, null).resetEndOfTurnModifiers(gd);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
    }

    @Test
    @DisplayName("Removes snow from a permanent until end of turn")
    void removesSnowFromPermanent() {
        Permanent target = addPermanent(true);

        castThermalFlux(1, target);

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
        new TurnCleanupService(null, null).resetEndOfTurnModifiers(gd);
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
    @DisplayName("Draws a card at the next upkeep")
    void drawsAtNextUpkeep() {
        Permanent target = addPermanent(false);
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        castThermalFlux(0, target);

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    private void castThermalFlux(int mode, Permanent target) {
        harness.setHand(player1, List.of(new ThermalFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addPermanent(boolean snow) {
        Permanent permanent = new Permanent(new Island());
        if (snow) {
            TestCards.mutableCard(permanent).setSupertypes(
                    EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        }
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        return permanent;
    }
}
