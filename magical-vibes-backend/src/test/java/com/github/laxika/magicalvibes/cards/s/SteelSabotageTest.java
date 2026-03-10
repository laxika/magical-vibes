package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteelSabotageTest extends BaseCardTest {

    @Test
    @DisplayName("Steel Sabotage has a ChooseOneEffect with two options")
    void hasCorrectEffects() {
        SteelSabotage card = new SteelSabotage();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ChooseOneEffect.class);
        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.options()).hasSize(2);
    }

    @Nested
    @DisplayName("Mode 1: Counter target artifact spell")
    class CounterMode {

        @Test
        @DisplayName("Counters target artifact spell")
        void countersArtifactSpell() {
            Millstone millstone = new Millstone();
            harness.setHand(player1, List.of(millstone));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castArtifact(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, millstone.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Millstone"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Millstone"));
        }

        @Test
        @DisplayName("Cannot target a creature spell with counter mode")
        void cannotTargetCreatureSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(bears));
            harness.addMana(player1, ManaColor.GREEN, 2);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castCreature(player1, 0);
            harness.passPriority(player1);

            assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Steel Sabotage goes to graveyard after countering")
        void goesToGraveyardAfterResolving() {
            Millstone millstone = new Millstone();
            harness.setHand(player1, List.of(millstone));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.castArtifact(player1, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, 0, millstone.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Steel Sabotage"));
        }
    }

    @Nested
    @DisplayName("Mode 2: Return target artifact to its owner's hand")
    class BounceMode {

        @Test
        @DisplayName("Returns target artifact to its owner's hand")
        void returnsArtifactToHand() {
            Millstone millstone = new Millstone();
            harness.addToBattlefield(player1, millstone);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            Permanent millstonePermanent = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Millstone"))
                    .findFirst().orElseThrow();

            harness.castInstant(player2, 0, 1, millstonePermanent.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Millstone"));
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Millstone"));
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature with bounce mode")
        void cannotTargetNonArtifactCreature() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.addToBattlefield(player1, bears);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            Permanent bearsPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();

            assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, bearsPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Steel Sabotage goes to graveyard after bouncing")
        void goesToGraveyardAfterResolving() {
            Millstone millstone = new Millstone();
            harness.addToBattlefield(player1, millstone);

            harness.setHand(player2, List.of(new SteelSabotage()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            Permanent millstonePermanent = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Millstone"))
                    .findFirst().orElseThrow();

            harness.castInstant(player2, 0, 1, millstonePermanent.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Steel Sabotage"));
        }
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new SteelSabotage()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 99, millstone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }
}
