package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class IvoryCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: All creatures get -2/-0 until end of turn")
    class WeakenAllMode {

        @Test
        @DisplayName("Weakens creatures on both battlefields")
        void weakensEveryCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            Permanent mine = findPermanent(player1, "Grizzly Bears");
            Permanent theirs = findPermanent(player2, "Grizzly Bears");
            assertThat(gqs.getEffectivePower(gd, mine)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(2);
            assertThat(gqs.getEffectivePower(gd, theirs)).isZero();
        }

        @Test
        @DisplayName("Wears off at the cleanup step")
        void wearsOff() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Mode 1: Tap target creature")
    class TapMode {

        @Test
        @DisplayName("Taps the targeted creature")
        void tapsTarget() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
        }
    }

    @Nested
    @DisplayName("Mode 2: Prevent the next 1 damage to any target")
    class PreventDamageMode {

        @Test
        @DisplayName("Adds a 1-damage prevention shield to a target creature")
        void shieldOnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(findPermanent(player1, "Grizzly Bears").getDamagePreventionShield()).isEqualTo(1);
        }

        @Test
        @DisplayName("Adds a 1-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new IvoryCharm()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 2, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        }
    }
}
