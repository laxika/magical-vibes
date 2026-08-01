package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

class GolgariCharmTest extends BaseCardTest {

    private void addBG() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Nested
    @DisplayName("Mode 0: All creatures get -1/-1 until end of turn")
    class DebuffMode {

        @Test
        @DisplayName("Debuffs creatures on both sides")
        void debuffsAllCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            Permanent own = findPermanent(player1, "Grizzly Bears");
            Permanent opp = findPermanent(player2, "Grizzly Bears");
            assertThat(own.getPowerModifier()).isEqualTo(-1);
            assertThat(own.getToughnessModifier()).isEqualTo(-1);
            assertThat(opp.getPowerModifier()).isEqualTo(-1);
            assertThat(opp.getToughnessModifier()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Debuff wears off at end of turn")
        void wearsOffAtEndOfTurn() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bears = findPermanent(player1, "Grizzly Bears");
            assertThat(bears.getPowerModifier()).isEqualTo(0);
            assertThat(bears.getToughnessModifier()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target enchantment")
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new Pacifism());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            UUID targetId = harness.getPermanentId(player2, "Pacifism");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Pacifism");
        }

        @Test
        @DisplayName("Cannot target a creature")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new Pacifism());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Regenerate each creature you control")
    class RegenerateMode {

        @Test
        @DisplayName("Gives regeneration shields to your creatures only")
        void regeneratesOwnCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new GolgariCharm()));
            addBG();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            Permanent own = findPermanent(player1, "Grizzly Bears");
            Permanent opp = findPermanent(player2, "Grizzly Bears");
            assertThat(own.getRegenerationShield()).isGreaterThan(0);
            assertThat(opp.getRegenerationShield()).isEqualTo(0);
        }
    }
}
