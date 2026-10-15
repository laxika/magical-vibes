package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TormentorExarch.class, GrizzlyBears.class, HillGiant.class})
class TormentorExarchTest extends BaseCardTest {

    @CardUsed({TormentorExarch.class, GrizzlyBears.class})
    @Nested
    @DisplayName("Mode 1: Target creature gets +2/+0 until end of turn")
    class BoostMode {

        @Test
        @DisplayName("Gives +2/+0 to target creature")
        void boostsTargetCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            UUID targetId = bears.getId();

            castWithBoostMode(targetId);
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(bears.getPowerModifier()).isEqualTo(2);
            assertThat(bears.getToughnessModifier()).isEqualTo(0);
            assertThat(bears.getEffectivePower()).isEqualTo(4);
            assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Tormentor Exarch enters the battlefield when choosing boost mode")
        void exarchEntersBattlefield() {
            UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

            castWithBoostMode(targetId);

            harness.assertOnBattlefield(player1, "Tormentor Exarch");
        }
    }

    @CardUsed({TormentorExarch.class, GrizzlyBears.class, HillGiant.class})
    @Nested
    @DisplayName("Mode 2: Target creature gets -0/-2 until end of turn")
    class DebuffMode {

        @Test
        @DisplayName("Gives -0/-2 to target creature")
        void debuffsTargetCreature() {
            Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
            UUID targetId = giant.getId();

            castWithDebuffMode(targetId);
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(giant.getPowerModifier()).isEqualTo(0);
            assertThat(giant.getToughnessModifier()).isEqualTo(-2);
            assertThat(giant.getEffectivePower()).isEqualTo(3);
            assertThat(giant.getEffectiveToughness()).isEqualTo(1);
        }

        @Test
        @DisplayName("Tormentor Exarch enters the battlefield when choosing debuff mode")
        void exarchEntersBattlefield() {
            UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

            castWithDebuffMode(targetId);

            harness.assertOnBattlefield(player1, "Tormentor Exarch");
        }

        @Test
        @DisplayName("-0/-2 kills a creature with 2 or less toughness")
        void debuffKillsLowToughnessCreature() {
            UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

            castWithDebuffMode(targetId);
            harness.passBothPriorities(); // resolve ETB trigger

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }
    }

    @Test
    @CardUsed({TormentorExarch.class})
    void choosesBoostModeAfterEnteringAndCanTargetItself() {
        harness.setHand(player1, List.of(new TormentorExarch()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent exarch = findPermanent(player1, "Tormentor Exarch");
        chooseTriggeredMode("Target creature gets +2/+0 until end of turn");
        harness.handlePermanentChosen(player1, exarch.getId());
        resolveAllTriggers();

        assertThat(exarch.getPowerModifier()).isEqualTo(2);
        assertThat(exarch.getToughnessModifier()).isZero();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(exarch.getPowerModifier()).isZero();
    }

    @Test
    @CardUsed({TormentorExarch.class})
    void canChooseDebuffModeWhenEnteringWithoutBeingCast() {
        Permanent exarch = harness.enterBattlefieldAndReturn(player1, new TormentorExarch());

        chooseTriggeredMode("Target creature gets -0/-2 until end of turn");
        harness.handlePermanentChosen(player1, exarch.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tormentor Exarch");
        harness.assertInGraveyard(player1, "Tormentor Exarch");
    }

    @Test
    @CardUsed({TormentorExarch.class, HillGiant.class})
    void debuffExpiresAtEndOfTurn() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castWithDebuffMode(giant.getId());
        resolveAllTriggers();

        assertThat(giant.getToughnessModifier()).isEqualTo(-2);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(giant.getToughnessModifier()).isZero();
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    private void chooseTriggeredMode(String mode) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, mode);
    }

    private void castWithBoostMode(UUID targetId) {
        harness.setHand(player1, List.of(new TormentorExarch()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseTriggeredMode("Target creature gets +2/+0 until end of turn");
        harness.handlePermanentChosen(player1, targetId);
    }

    private void castWithDebuffMode(UUID targetId) {
        harness.setHand(player1, List.of(new TormentorExarch()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseTriggeredMode("Target creature gets -0/-2 until end of turn");
        harness.handlePermanentChosen(player1, targetId);
    }
}
