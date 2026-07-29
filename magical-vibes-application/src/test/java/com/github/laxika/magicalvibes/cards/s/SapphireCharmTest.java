package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SapphireCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target player draws a card at the beginning of the next turn's upkeep")
    class DelayedDrawMode {

        @Test
        @DisplayName("Schedules the draw for the targeted player, not the caster")
        void schedulesForTargetedPlayer() {
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            int handBefore = gd.playerHands.get(player2.getId()).size();

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);

            List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
            assertThat(scheduled).hasSize(1);
            assertThat(scheduled.getFirst().controllerId()).isEqualTo(player2.getId());
            assertThat(scheduled.getFirst().count()).isEqualTo(1);
        }

        @Test
        @DisplayName("The targeted player draws at the next upkeep")
        void drawResolvesAtNextUpkeep() {
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            int handBefore = gd.playerHands.get(player2.getId()).size();
            int deckBefore = gd.playerDecks.get(player2.getId()).size();

            StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
            gd.activePlayerId = player2.getId();
            harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

            assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
            assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Mode 1: Target creature gains flying until end of turn")
    class FlyingMode {

        @Test
        @DisplayName("Grants flying to the targeted creature")
        void grantsFlying() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("Flying wears off at the cleanup step")
        void flyingWearsOff() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FLYING)).isFalse();
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature an opponent controls phases out")
    class PhaseOutMode {

        @Test
        @DisplayName("Phases out the opponent's creature")
        void phasesOutOpponentCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.phasedOutPermanents.get(player2.getId()))
                    .anyMatch(permanent -> permanent.getId().equals(targetId));
        }

        @Test
        @DisplayName("Cannot target a creature its own controller controls")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                    .hasMessageContaining("Target");
        }
    }
}
