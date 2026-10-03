package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiabolicServitude.class, GorillaWarrior.class, HeatRay.class, Disenchant.class})
class DiabolicServitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by returning a target creature card from your graveyard")
    void reanimatesTargetCreature() {
        Card gorilla = castServitudeAndReturnGorilla();

        assertThat(findPermanentByCardId(gorilla.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Gorilla Warrior");
        harness.assertOnBattlefield(player1, "Diabolic Servitude");
    }

    @Test
    @DisplayName("When the linked creature dies, it is exiled and the enchantment returns to hand")
    void linkedCreatureDeathExilesCreatureAndReturnsServitude() {
        Card gorilla = castServitudeAndReturnGorilla();
        Permanent reanimated = findPermanentByCardId(gorilla.getId());

        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 3, reanimated.getId());
        resolveAllTriggers();

        assertThat(findPermanentByCardId(gorilla.getId())).isNull();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(gorilla.getId())
                && entry.ownerId().equals(player1.getId()));
        harness.assertInHand(player1, "Diabolic Servitude");
    }

    @Test
    @DisplayName("When the enchantment leaves, the linked creature is exiled")
    void leavingExilesLinkedCreature() {
        Card gorilla = castServitudeAndReturnGorilla();
        Permanent servitude = findPermanent(player1, "Diabolic Servitude");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, servitude));
        harness.clearPriorityPassed();
        resolveAllTriggers();

        assertThat(findPermanentByCardId(gorilla.getId())).isNull();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(gorilla.getId())
                && entry.ownerId().equals(player1.getId()));
        harness.assertInGraveyard(player1, "Diabolic Servitude");
    }

    @Test
    @DisplayName("The leave trigger loses track of a creature that dies before it resolves")
    void leavingDoesNotExileLinkedCardFromGraveyard() {
        Card gorilla = castServitudeAndReturnGorilla();
        Permanent servitude = findPermanent(player1, "Diabolic Servitude");
        Permanent reanimated = findPermanentByCardId(gorilla.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, servitude));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 3, reanimated.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gorilla Warrior");
        harness.passBothPriorities();

        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(gorilla.getId()));
        harness.assertInGraveyard(player1, "Gorilla Warrior");
        harness.assertInGraveyard(player1, "Diabolic Servitude");
    }

    @Test
    @DisplayName("A different creature dying does not break the link")
    void unrelatedCreatureDeathDoesNothing() {
        Card gorilla = castServitudeAndReturnGorilla();
        Card unrelatedCard = new GorillaWarrior();
        harness.addToBattlefield(player1, unrelatedCard);
        Permanent unrelated = findPermanentByCardId(unrelatedCard.getId());

        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 3, unrelated.getId());
        resolveAllTriggers();

        assertThat(findPermanentByCardId(gorilla.getId())).isNotNull();
        harness.assertOnBattlefield(player1, "Diabolic Servitude");
        harness.assertInGraveyard(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNonCreatureCard() {
        Card disenchant = new Disenchant();
        Card gorilla = new GorillaWarrior();
        harness.setGraveyard(player1, List.of(disenchant, gorilla));
        harness.setHand(player1, List.of(new DiabolicServitude()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(disenchant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
        harness.handleMultipleCardsChosen(player1, List.of(gorilla.getId()));
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Disenchant");
        assertThat(findPermanentByCardId(gorilla.getId())).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentCreatureCard() {
        Card gorilla = new GorillaWarrior();
        harness.setGraveyard(player2, List.of(gorilla));
        Card ownGorilla = new GorillaWarrior();
        harness.setGraveyard(player1, List.of(ownGorilla));
        harness.setHand(player1, List.of(new DiabolicServitude()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(gorilla.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");
        harness.handleMultipleCardsChosen(player1, List.of(ownGorilla.getId()));
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Gorilla Warrior");
        assertThat(findPermanentByCardId(ownGorilla.getId())).isNotNull();
    }

    @Test
    @DisplayName("Destroying Servitude in response to the death trigger keeps it in the graveyard")
    void destroyedServitudeDoesNotReturnFromGraveyard() {
        Card gorilla = castServitudeAndReturnGorilla();
        Permanent reanimated = findPermanentByCardId(gorilla.getId());
        Permanent servitude = findPermanent(player1, "Diabolic Servitude");
        harness.setHand(player1, List.of(new HeatRay(), new Disenchant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 3, reanimated.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Gorilla Warrior");
        harness.castAndResolveInstant(player1, 0, servitude.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Diabolic Servitude");
        harness.assertNotInHand(player1, "Diabolic Servitude");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(gorilla.getId()));
    }

    @Test
    @DisplayName("Removing Servitude before its enter trigger resolves still returns the creature")
    void enterTriggerResolvesAfterServitudeLeaves() {
        Card gorilla = new GorillaWarrior();
        harness.setGraveyard(player1, List.of(gorilla));
        harness.setHand(player1, List.of(new DiabolicServitude(), new Disenchant()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(gorilla.getId()));

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Diabolic Servitude"));
        resolveAllTriggers();

        assertThat(findPermanentByCardId(gorilla.getId())).isNotNull();
        harness.assertInGraveyard(player1, "Diabolic Servitude");
        harness.assertNotInGraveyard(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Servitude can enter with no creature cards in the graveyard")
    void entersWithNoLegalGraveyardTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new DiabolicServitude()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Diabolic Servitude");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Card castServitudeAndReturnGorilla() {
        Card gorilla = new GorillaWarrior();
        harness.setGraveyard(player1, List.of(gorilla));
        harness.setHand(player1, List.of(new DiabolicServitude()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(gorilla.getId()));
        harness.passBothPriorities();
        return gorilla;
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
