package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PurgatoryTest extends BaseCardTest {

    @Nested
    @DisplayName("Nontoken creature death exile trigger")
    class DeathTrigger {

        @Test
        @DisplayName("A dying nontoken creature is exiled with the enchantment instead of staying in the graveyard")
        void dyingCreatureIsExiledWithEnchantment() {
            UUID permId = addPurgatory();
            harness.addToBattlefield(player1, new GrizzlyBears());

            castWrath();

            assertThat(gd.getCardsExiledByPermanent(permId))
                    .extracting(Card::getName).containsExactly("Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
        }
    }

    @Nested
    @DisplayName("Upkeep: pay {4} and 2 life to return an exiled card")
    class UpkeepReturn {

        @Test
        @DisplayName("Accepting pays 2 life and returns the exiled card to the battlefield")
        void acceptingReturnsCardAndPaysLife() {
            UUID permId = setupWithExiledBears(1);
            int lifeBefore = gd.getLife(player1.getId());

            advanceToSecondTurnUpkeep(player1);
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.passBothPriorities(); // resolve upkeep trigger → may-pay prompt
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        }

        @Test
        @DisplayName("Declining leaves the card exiled and costs no life")
        void decliningLeavesCardExiled() {
            UUID permId = setupWithExiledBears(1);
            int lifeBefore = gd.getLife(player1.getId());

            advanceToSecondTurnUpkeep(player1);
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("Without enough mana the payment fails and the card stays exiled")
        void withoutManaPaymentFails() {
            UUID permId = setupWithExiledBears(1);

            advanceToSecondTurnUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
        }

        @Test
        @DisplayName("With only 1 life the 2-life half of the cost cannot be paid, so nothing returns")
        void withoutEnoughLifePaymentFails() {
            UUID permId = setupWithExiledBears(1);
            gd.playerLifeTotals.put(player1.getId(), 1);

            advanceToSecondTurnUpkeep(player1);
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
            assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        }

        @Test
        @DisplayName("With several exiled cards, the controller chooses which one returns")
        void severalExiledCardsOfferAChoice() {
            UUID permId = setupWithExiledBears(3);
            UUID chosen = gd.getCardsExiledByPermanent(permId).getFirst().getId();

            advanceToSecondTurnUpkeep(player1);
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
            harness.handleMultipleCardsChosen(player1, List.of(chosen));

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getId().equals(chosen));
            assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(2);
            assertThat(gd.getCardsExiledByPermanent(permId)).noneMatch(c -> c.getId().equals(chosen));
        }
    }

    private UUID addPurgatory() {
        harness.addToBattlefield(player1, new Purgatory());
        return harness.getPermanentId(player1, "Purgatory");
    }

    private UUID setupWithExiledBears(int count) {
        UUID permId = addPurgatory();
        for (int i = 0; i < count; i++) {
            gd.addToExile(player1.getId(), new GrizzlyBears(), permId);
        }
        return permId;
    }

    private void castWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath — creatures die
        harness.passBothPriorities(); // resolve the exile death trigger
    }

    private void advanceToSecondTurnUpkeep(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
    }
}
