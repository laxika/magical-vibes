package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BantCharm.class, GrizzlyBears.class, Millstone.class, Shock.class, Ornithopter.class})
class BantCharmTest extends BaseCardTest {

    private void addGWU() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Nested
    @DisplayName("Mode 0: Destroy target artifact")
    @CardUsed({BantCharm.class, Millstone.class, GrizzlyBears.class, Ornithopter.class})
    class DestroyArtifactMode {

        @Test
        void destroysArtifactCreature() {
            var target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            harness.castInstant(player1, 0, 0, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Ornithopter");
            harness.assertInGraveyard(player2, "Ornithopter");
        }

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            UUID targetId = harness.getPermanentId(player2, "Millstone");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
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
    @CardUsed({BantCharm.class, GrizzlyBears.class, Millstone.class, Ornithopter.class, Shock.class})
    class BottomOfLibraryMode {

        @Test
        void doesNotMoveCreatureThatDiedBeforeResolution() {
            var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            int deckSize = gd.playerDecks.get(player2.getId()).size();
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();
            harness.castInstant(player1, 0, 1, target.getId());

            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.castInstant(player2, 0, target.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Grizzly Bears");
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSize);
            harness.assertInGraveyard(player1, "Bant Charm");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        void putsStolenCreatureInOwnersLibrary() {
            var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            gd.stolenCreatures.put(target.getId(), player2.getId());
            int controllerDeckSize = gd.playerDecks.get(player1.getId()).size();
            int ownerDeckSize = gd.playerDecks.get(player2.getId()).size();
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(controllerDeckSize);
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(ownerDeckSize + 1);
            assertThat(gd.playerDecks.get(player2.getId()).getLast()).isSameAs(target.getCard());
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        void putsArtifactCreatureOnBottomWithoutDestroyingIt() {
            var target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            harness.castInstant(player1, 0, 1, target.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Ornithopter");
            harness.assertNotInGraveyard(player2, "Ornithopter");
            assertThat(gd.playerDecks.get(player2.getId()).getLast()).isSameAs(target.getCard());
        }

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

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertNotInGraveyard(player2, "Grizzly Bears");
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
    @CardUsed({BantCharm.class, Shock.class, GrizzlyBears.class})
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
            harness.assertInGraveyard(player2, "Shock");
        }

        @Test
        @DisplayName("Cannot counter a non-instant spell")
        void cannotCounterCreatureSpell() {
            GrizzlyBears bears = new GrizzlyBears();
            harness.setHand(player1, List.of(new BantCharm()));
            addGWU();

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, bears, "{1}{G}");
            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
