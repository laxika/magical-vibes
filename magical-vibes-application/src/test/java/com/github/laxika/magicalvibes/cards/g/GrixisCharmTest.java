package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrixisCharmTest extends BaseCardTest {

    private void addUBR() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Nested
    @DisplayName("Mode 0: Return target permanent to its owner's hand")
    class BounceMode {

        @Test
        @DisplayName("Returns any permanent to its owner's hand")
        void returnsPermanent() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
            assertThat(gd.playerHands.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Fountain of Youth"));
        }
    }

    @Nested
    @DisplayName("Mode 1: Target creature gets -4/-4 until end of turn")
    class DebuffMode {

        @Test
        @DisplayName("Kills a 2/2 creature")
        void killsSmallCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Creatures you control get +2/+0 until end of turn")
    class PumpMode {

        @Test
        @DisplayName("Boosts own creatures' power")
        void boostsOwnCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(bear.getEffectivePower()).isEqualTo(4);
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Boost wears off at cleanup step")
        void boostWearsOff() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new GrixisCharm()));
            addUBR();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(bear.getEffectivePower()).isEqualTo(2);
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        }
    }
}
