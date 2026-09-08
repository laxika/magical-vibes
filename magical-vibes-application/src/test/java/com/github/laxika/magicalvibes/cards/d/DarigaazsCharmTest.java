package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

class DarigaazsCharmTest extends BaseCardTest {

    private void addBRG() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Nested
    @DisplayName("Mode 0: Return target creature card from your graveyard to your hand")
    class ReturnCreatureMode {

        @Test
        @DisplayName("Returns a creature card from the graveyard to hand")
        void returnsCreatureToHand() {
            Card creature = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(creature));
            harness.setHand(player1, List.of(new DarigaazsCharm()));
            addBRG();

            harness.castInstant(player1, 0, 0, creature.getId());
            harness.passBothPriorities();

            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a noncreature card in a graveyard")
        void cannotTargetNoncreatureCard() {
            Card instant = new GiantGrowth();
            harness.setGraveyard(player1, List.of(instant));
            harness.setHand(player1, List.of(new DarigaazsCharm()));
            addBRG();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, instant.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Deal 3 damage to any target")
    class DamageMode {

        @Test
        @DisplayName("Deals 3 damage to the targeted player")
        void damagesPlayer() {
            harness.setHand(player1, List.of(new DarigaazsCharm()));
            addBRG();

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature gets +3/+3 until end of turn")
    class BoostMode {

        @Test
        @DisplayName("Boosts a creature until end of turn")
        void boostsCreatureUntilEndOfTurn() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new DarigaazsCharm()));
            addBRG();

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            Permanent bears = findPermanent(player1, "Grizzly Bears");
            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreaturePermanent() {
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new DarigaazsCharm()));
            addBRG();

            UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("creature");
        }
    }
}
