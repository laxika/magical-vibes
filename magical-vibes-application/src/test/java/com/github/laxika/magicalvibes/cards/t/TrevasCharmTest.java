package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.cards.m.MorgueToad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrevasCharm.class, CloudCover.class, MorgueToad.class})
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
            harness.addToBattlefield(player2, new CloudCover());
            harness.setHand(player1, List.of(new TrevasCharm()));
            addGUW();

            Permanent cloudCover = findPermanent(player2, "Cloud Cover");
            harness.castModalInstant(player1, 0, 0, List.of(cloudCover.getId()));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Cloud Cover");
            harness.assertInGraveyard(player2, "Cloud Cover");
        }

        @Test
        @DisplayName("Cannot target a creature with the enchantment mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new MorgueToad());
            harness.setHand(player1, List.of(new TrevasCharm()));
            addGUW();

            Permanent toad = findPermanent(player2, "Morgue Toad");
            assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(toad.getId())))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Exile target attacking creature")
    class ExileAttackerMode {

        @Test
        @DisplayName("Exiles target attacking creature")
        void exilesAttackingCreature() {
            Permanent attacker = addAttacker(player2, player1, new MorgueToad());
            harness.setHand(player1, List.of(new TrevasCharm()));
            addGUW();

            harness.castModalInstant(player1, 0, 1, List.of(attacker.getId()));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Morgue Toad");
            assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Morgue Toad"));
        }

        @Test
        @DisplayName("Cannot target a nonattacking creature")
        void cannotTargetNonattacker() {
            Permanent attacker = addAttacker(player2, player1, new MorgueToad());
            Permanent nonattacker = harness.addToBattlefieldAndReturn(player2, new MorgueToad());
            harness.setHand(player1, List.of(new TrevasCharm()));
            addGUW();

            assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(nonattacker.getId())))
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
            harness.setHand(player1, List.of(new TrevasCharm(), new CloudCover()));
            harness.setLibrary(player1, List.of(new MorgueToad()));
            addGUW();

            harness.castModalInstant(player1, 0, 2, List.of());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(card -> card.getName().equals("Morgue Toad"));

            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            harness.assertInGraveyard(player1, "Cloud Cover");
        }
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent attacker = addCreatureReady(controller, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }
}
