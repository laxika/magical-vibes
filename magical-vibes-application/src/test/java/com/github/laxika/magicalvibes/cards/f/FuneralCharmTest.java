package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FuneralCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target player discards a card")
    class DiscardMode {

        @Test
        @DisplayName("Targeted player discards a card")
        void targetDiscards() {
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel())));

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player2, 0);

            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        }

        @Test
        @DisplayName("Can target yourself")
        void canTargetSelf() {
            harness.setHand(player1, new ArrayList<>(List.of(new FuneralCharm(), new GrizzlyBears())));
            harness.addMana(player1, ManaColor.BLACK, 1);

            harness.castInstant(player1, 0, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            harness.assertInGraveyard(player1, "Grizzly Bears");
        }
    }

    @Nested
    @DisplayName("Mode 1: Target creature gets +2/-1 until end of turn")
    class BoostMode {

        @Test
        @DisplayName("Gives +2/-1")
        void boostsTarget() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            Permanent bear = permanent(targetId);
            assertThat(bear.getEffectivePower()).isEqualTo(4);
            assertThat(bear.getEffectiveToughness()).isEqualTo(1);
        }

        @Test
        @DisplayName("Boost wears off at end of turn")
        void boostWearsOff() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            Permanent bear = permanent(targetId);
            assertThat(bear.getEffectivePower()).isEqualTo(2);
            assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player1, new FountainOfYouth());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature gains swampwalk until end of turn")
    class SwampwalkMode {

        @Test
        @DisplayName("Grants swampwalk")
        void grantsSwampwalk() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, permanent(targetId), Keyword.SWAMPWALK)).isTrue();
        }

        @Test
        @DisplayName("Swampwalk wears off at end of turn")
        void swampwalkWearsOff() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new FuneralCharm()));
            harness.addMana(player1, ManaColor.BLACK, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, permanent(targetId), Keyword.SWAMPWALK)).isFalse();
        }
    }

    private Permanent permanent(UUID id) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
    }
}
