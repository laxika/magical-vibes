package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrevasCharmTest extends BaseCardTest {

    private void addGUW() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Nested
    @DisplayName("Mode 0: Destroy target enchantment")
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new GloriousAnthem());
            harness.setHand(player1, List.of(new TrevasCharm()));
            addGUW();

            Permanent anthem = findPermanent(player2, "Glorious Anthem");
            harness.castInstant(player1, 0, 0, anthem.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Glorious Anthem");
            harness.assertInGraveyard(player2, "Glorious Anthem");
        }

        @Test
        @DisplayName("Cannot target a creature with the enchantment mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new GloriousAnthem());
            harness.setHand(player1, List.of(new TrevasCharm()));
            addGUW();

            Permanent bears = findPermanent(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Exile target attacking creature")
    class ExileAttackerMode {

        @Test
        @DisplayName("Exiles target attacking creature")
        void exilesAttackingCreature() {
            Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new TrevasCharm()));
            addGUW();

            harness.castInstant(player1, 0, 1, attacker.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot target a nonattacking creature")
        void cannotTargetNonattacker() {
            Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());
            Permanent nonattacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new TrevasCharm()));
            addGUW();

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, nonattacker.getId()))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        }
    }

    @Nested
    @DisplayName("Mode 2: Draw a card, then discard a card")
    class LootMode {

        @Test
        @DisplayName("Draws before prompting for a discard")
        void drawsThenDiscards() {
            harness.setHand(player1, List.of(new TrevasCharm(), new Peek()));
            harness.setLibrary(player1, List.of(new GrizzlyBears()));
            addGUW();

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(card -> card.getName().equals("Grizzly Bears"));

            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            harness.assertInGraveyard(player1, "Peek");
        }
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(attacker);
        return attacker;
    }
}
