package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.g.GoblinScouts;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.SavageTwister;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Purgatory.class, FeralShadow.class, GoblinScouts.class, RayOfCommand.class, SavageTwister.class})
class PurgatoryTest extends BaseCardTest {

    @Nested
    @DisplayName("Nontoken creature death exile trigger")
    class DeathTrigger {

        @Test
        @DisplayName("A dying nontoken creature is exiled with the enchantment instead of staying in the graveyard")
        void dyingCreatureIsExiledWithEnchantment() {
            UUID permId = addPurgatory();
            harness.addToBattlefield(player1, new FeralShadow());

            castSavageTwister(player1, 2);

            assertThat(gd.getCardsExiledByPermanent(permId))
                    .extracting(Card::getName).containsExactly("Feral Shadow");
            harness.assertNotInGraveyard(player1, "Feral Shadow");
        }

        @Test
        @DisplayName("A dying token is not exiled with the enchantment")
        void dyingTokenIsNotExiled() {
            UUID permId = addPurgatory();

            harness.setHand(player1, List.of(new GoblinScouts()));
            harness.addMana(player1, ManaColor.RED, 5);
            harness.castAndResolveSorcery(player1, 0, 0);

            castSavageTwister(player1, 1);

            assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
            harness.assertNotOnBattlefield(player1, "Goblin Scout");
        }

        @Test
        @DisplayName("A creature owned by the controller and controlled by an opponent is exiled")
        void creatureOwnedByControllerButControlledByOpponentIsExiled() {
            UUID permId = addPurgatory();
            Permanent target = addCreatureReady(player1, new FeralShadow());

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            castRayOfCommand(player2, target);

            castSavageTwister(player2, 2);

            assertThat(gd.getCardsExiledByPermanent(permId))
                    .extracting(Card::getName).containsExactly("Feral Shadow");
            harness.assertNotInGraveyard(player1, "Feral Shadow");
        }

        @Test
        @DisplayName("A creature owned by an opponent and controlled by the controller is not exiled")
        void creatureOwnedByOpponentButControlledByControllerIsNotExiled() {
            UUID permId = addPurgatory();
            Permanent target = addCreatureReady(player2, new FeralShadow());

            castRayOfCommand(player1, target);
            castSavageTwister(player1, 2);

            assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
            harness.assertInGraveyard(player2, "Feral Shadow");
        }
    }

    @Nested
    @DisplayName("Upkeep: pay {4} and 2 life to return an exiled card")
    class UpkeepReturn {

        @Test
        @DisplayName("Accepting pays 2 life and returns the exiled card to the battlefield")
        void acceptingReturnsCardAndPaysLife() {
            UUID permId = setupWithExiledShadows(1);
            int lifeBefore = gd.getLife(player1.getId());

            advanceToSecondTurnUpkeep(player1);
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.passBothPriorities(); // resolve upkeep trigger → may-pay prompt
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Feral Shadow");
            assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        }

        @Test
        @DisplayName("Declining leaves the card exiled and costs no life")
        void decliningLeavesCardExiled() {
            UUID permId = setupWithExiledShadows(1);
            int lifeBefore = gd.getLife(player1.getId());

            advanceToSecondTurnUpkeep(player1);
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            harness.assertNotOnBattlefield(player1, "Feral Shadow");
            assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("Without enough mana the payment fails and the card stays exiled")
        void withoutManaPaymentFails() {
            UUID permId = setupWithExiledShadows(1);

            advanceToSecondTurnUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Feral Shadow");
            assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
        }

        @Test
        @DisplayName("With only 1 life the 2-life half of the cost cannot be paid, so nothing returns")
        void withoutEnoughLifePaymentFails() {
            UUID permId = setupWithExiledShadows(1);
            gd.playerLifeTotals.put(player1.getId(), 1);

            advanceToSecondTurnUpkeep(player1);
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Feral Shadow");
            assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
            assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        }

        @Test
        @DisplayName("With several exiled cards, the controller chooses which one returns")
        void severalExiledCardsOfferAChoice() {
            UUID permId = setupWithExiledShadows(3);
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

        @Test
        @DisplayName("Only a card exiled with this enchantment can be returned")
        void onlyCardsExiledWithThisEnchantmentCanBeReturned() {
            UUID permId = addPurgatory();
            Card unrelated = new FeralShadow();
            Card tracked = new FeralShadow();
            gd.addToExile(player1.getId(), unrelated);
            gd.addToExile(player1.getId(), tracked, permId);

            advanceToSecondTurnUpkeep(player1);
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getId().equals(tracked.getId()))
                    .noneMatch(p -> p.getCard().getId().equals(unrelated.getId()));
            assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
            assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(unrelated.getId()));
        }
    }

    private UUID addPurgatory() {
        harness.addToBattlefield(player1, new Purgatory());
        return harness.getPermanentId(player1, "Purgatory");
    }

    private UUID setupWithExiledShadows(int count) {
        UUID permId = addPurgatory();
        for (int i = 0; i < count; i++) {
            gd.addToExile(player1.getId(), new FeralShadow(), permId);
        }
        return permId;
    }

    private void castRayOfCommand(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new RayOfCommand()));
        harness.addMana(caster, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private void castSavageTwister(Player caster, int xValue) {
        harness.setHand(caster, List.of(new SavageTwister()));
        harness.addMana(caster, ManaColor.RED, xValue + 1);
        harness.addMana(caster, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(caster, 0, xValue);
        harness.passBothPriorities();
    }

    private void advanceToSecondTurnUpkeep(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
    }
}
