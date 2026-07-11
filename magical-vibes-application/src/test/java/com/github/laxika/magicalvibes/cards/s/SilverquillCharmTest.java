package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SilverquillCharmTest extends BaseCardTest {

    private void addWB() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    

    @Nested
    @DisplayName("Mode 0: Two +1/+1 counters on target creature")
    class CounterMode {

        @Test
        @DisplayName("Puts two +1/+1 counters on target creature")
        void putsTwoCounters() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SilverquillCharm()));
            addWB();

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Mode 1: Exile target creature with power 2 or less")
    class ExileMode {

        @Test
        @DisplayName("Exiles a creature with power 2 or less")
        void exilesLowPowerCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new SilverquillCharm()));
            addWB();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot target a creature with power greater than 2")
        void cannotTargetHighPowerCreature() {
            harness.addToBattlefield(player2, new EnormousBaloth());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SilverquillCharm()));
            addWB();

            UUID targetId = harness.getPermanentId(player2, "Enormous Baloth");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Each opponent loses 3 life and you gain 3 life")
    class DrainMode {

        @Test
        @DisplayName("Opponent loses 3, controller gains 3")
        void drainsLife() {
            harness.setHand(player1, List.of(new SilverquillCharm()));
            addWB();
            int opponentBefore = gd.playerLifeTotals.get(player2.getId());
            int controllerBefore = gd.playerLifeTotals.get(player1.getId());

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentBefore - 3);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerBefore + 3);
        }
    }
}
