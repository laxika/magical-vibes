package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IzzetCharmTest extends BaseCardTest {

    private void addUR() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    @Nested
    @DisplayName("Mode 0: Counter target noncreature spell unless controller pays {2}")
    class CounterMode {

        @Test
        @DisplayName("Counters noncreature spell when opponent cannot pay {2}")
        void countersWhenCannotPay() {
            LightningBolt bolt = new LightningBolt();
            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(bolt));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.setHand(player1, List.of(new IzzetCharm()));
            addUR();

            harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, bolt.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player2, "Lightning Bolt");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does not counter when opponent pays {2}")
        void doesNotCounterWhenPays() {
            GiantGrowth growth = new GiantGrowth();
            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(growth));
            harness.addMana(player2, ManaColor.GREEN, 3);
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new IzzetCharm()));
            addUR();

            harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passPriority(player2);
            harness.castInstant(player1, 0, 0, growth.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player2, true);

            harness.passBothPriorities();
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectivePower()).isEqualTo(5);
            harness.assertInGraveyard(player2, "Giant Growth");
            harness.assertInGraveyard(player1, "Izzet Charm");
        }

        @Test
        @DisplayName("Cannot target a creature spell")
        void cannotTargetCreatureSpell() {
            LlanowarElves elves = new LlanowarElves();
            harness.forceActivePlayer(player2);
            harness.setHand(player2, List.of(elves));
            harness.addMana(player2, ManaColor.GREEN, 1);

            harness.setHand(player1, List.of(new IzzetCharm()));
            addUR();

            harness.castCreature(player2, 0);
            harness.passPriority(player2);
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, elves.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Deals 2 damage to target creature")
    class DamageMode {

        @Test
        @DisplayName("Kills a 2/2")
        void dealsTwoDamage() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new IzzetCharm()));
            addUR();

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target a noncreature permanent")
        void cannotTargetNoncreature() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            harness.setHand(player1, List.of(new IzzetCharm()));
            addUR();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                    harness.getPermanentId(player2, "Fountain of Youth")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Draw two cards, then discard two cards")
    class LootMode {

        @Test
        @DisplayName("Draws two then discards two")
        void drawsTwoThenDiscardsTwo() {
            setDeck(player1, List.of(new Island(), new Island()));
            harness.setHand(player1, List.of(new IzzetCharm(), new GrizzlyBears(), new GrizzlyBears()));
            addUR();

            harness.castInstant(player1, 0, 2, (java.util.UUID) null);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
            harness.assertInGraveyard(player1, "Izzet Charm");
        }
    }
}
