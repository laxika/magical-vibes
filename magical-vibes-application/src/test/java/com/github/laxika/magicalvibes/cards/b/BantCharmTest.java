package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BantCharmTest extends BaseCardTest {

    private void addGWU() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Nested
    @DisplayName("Mode 0: Destroy target artifact")
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            UUID targetId = harness.getPermanentId(player2, "Millstone");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Millstone"));
        }

        @Test
        @DisplayName("Cannot target a creature with the artifact mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new Millstone());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Put target creature on the bottom of its owner's library")
    class BottomOfLibraryMode {

        @Test
        @DisplayName("Puts target creature on the bottom of its owner's library")
        void putsCreatureOnBottom() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            List<Card> deck = gd.playerDecks.get(player2.getId());
            assertThat(deck).hasSize(deckSizeBefore + 1);
            assertThat(deck.getLast().getName()).isEqualTo("Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target an artifact with the creature mode")
        void cannotTargetArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            UUID targetId = harness.getPermanentId(player2, "Millstone");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Counter target instant spell")
    class CounterInstantMode {

        @Test
        @DisplayName("Counters a target instant spell")
        void countersInstant() {
            Shock shock = new Shock();
            harness.setHand(player2, List.of(shock));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            harness.forceActivePlayer(player2);
            harness.castInstant(player2, 0, player1.getId());
            harness.passPriority(player2);

            harness.castInstant(player1, 0, 2, shock.getId());
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Shock"));
        }

        @Test
        @DisplayName("Cannot counter a non-instant spell")
        void cannotCounterCreatureSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player2, List.of(bears));
            harness.addMana(player2, ManaColor.GREEN, 2);
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            harness.forceActivePlayer(player2);
            harness.castCreature(player2, 0);
            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
