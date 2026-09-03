package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SapphireCharm.class, GiantMantis.class})
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

            advanceToUpkeep(player2);

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
            harness.addToBattlefield(player1, new GiantMantis());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player1, "Giant Mantis");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Giant Mantis"), Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("Grants flying to an opponent's targeted creature")
        void grantsFlyingToOpponentsCreature() {
            harness.addToBattlefield(player2, new GiantMantis());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player2, "Giant Mantis");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Giant Mantis"), Keyword.FLYING)).isTrue();
        }

        @Test
        @DisplayName("Flying wears off at the cleanup step")
        void flyingWearsOff() {
            harness.addToBattlefield(player1, new GiantMantis());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player1, "Giant Mantis");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Giant Mantis"), Keyword.FLYING)).isFalse();
        }
    }

    @Nested
    @DisplayName("Mode 2: Target creature an opponent controls phases out")
    class PhaseOutMode {

        @Test
        @DisplayName("Phases out the opponent's creature")
        void phasesOutOpponentCreature() {
            harness.addToBattlefield(player2, new GiantMantis());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player2, "Giant Mantis");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Giant Mantis");
            assertThat(gd.phasedOutPermanents.get(player2.getId()))
                    .anyMatch(permanent -> permanent.getId().equals(targetId));
        }

        @Test
        @DisplayName("Phased-out creature phases in during its controller's next untap step")
        void phasesBackInAtNextUntap() {
            var creature = harness.addToBattlefieldAndReturn(player2, new GiantMantis());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castInstant(player1, 0, 2, creature.getId());
            harness.passBothPriorities();

            assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);

            advanceToUpkeep(player2);

            assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
            assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of()))
                    .doesNotContain(creature);
        }

        @Test
        @DisplayName("Cannot target a creature its own controller controls")
        void cannotTargetOwnCreature() {
            harness.addToBattlefield(player1, new GiantMantis());
            harness.setHand(player1, List.of(new SapphireCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player1, "Giant Mantis");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                    .hasMessageContaining("Target");
        }
    }
}
