package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverwalkTechnique.class, GrizzlyBears.class, Island.class, LightningBolt.class})
class RiverwalkTechniqueTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Puts target nonland permanent on top or bottom of its owner's library")
    class LibraryMode {

        @Test
        @DisplayName("The permanent's owner can choose the top of their library")
        void ownerChoosesTop() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Card libraryCard = new Island();
            setLibrary(player2, List.of(libraryCard));

            castMode(0, target.getId());
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);

            harness.handleListChoice(player2, "Top");

            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("The permanent's owner can choose the bottom of their library")
        void ownerChoosesBottom() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            Card libraryCard = new Island();
            setLibrary(player2, List.of(libraryCard));

            castMode(0, target.getId());
            harness.handleListChoice(player2, "Bottom");

            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, target.getCard());
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a land")
        void cannotTargetLand() {
            Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
            prepareCard();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, land.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Counters target noncreature spell")
    class CounterMode {

        @Test
        @DisplayName("Counters a noncreature spell")
        void countersNoncreatureSpell() {
            LightningBolt bolt = new LightningBolt();
            Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(bolt));
            harness.addMana(player2, ManaColor.RED, 1);
            prepareCard();

            harness.castInstant(player2, 0, target.getId());
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 1, bolt.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Lightning Bolt");
            harness.assertInGraveyard(player1, "Riverwalk Technique");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Cannot target a creature spell")
        void cannotTargetCreatureSpell() {
            GrizzlyBears creature = new GrizzlyBears();
            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(creature));
            harness.addMana(player2, ManaColor.GREEN, 2);
            prepareCard();

            harness.castCreature(player2, 0);
            harness.passPriority(player2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void castMode(int mode, java.util.UUID targetId) {
        prepareCard();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new RiverwalkTechnique()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
