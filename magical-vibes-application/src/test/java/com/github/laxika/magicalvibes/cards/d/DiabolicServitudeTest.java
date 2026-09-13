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
    @DisplayName("The leave trigger exiles the linked card if it dies before that trigger resolves")
    void leavingExilesLinkedCardFromGraveyard() {
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

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(gorilla.getId())
                && entry.ownerId().equals(player1.getId()));
        harness.assertNotInGraveyard(player1, "Gorilla Warrior");
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
        harness.setGraveyard(player1, List.of(disenchant));
        harness.setHand(player1, List.of(new DiabolicServitude()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, disenchant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentCreatureCard() {
        Card gorilla = new GorillaWarrior();
        harness.setGraveyard(player2, List.of(gorilla));
        harness.setHand(player1, List.of(new DiabolicServitude()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, gorilla.getId()))
                .isInstanceOf(IllegalStateException.class);
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
