package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiabolicServitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by returning a target creature card from your graveyard")
    void reanimatesTargetCreature() {
        Card bears = castServitudeAndReturnBears();

        assertThat(findPermanentByCardId(bears.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Diabolic Servitude");
    }

    @Test
    @DisplayName("When the linked creature dies, it is exiled and the enchantment returns to hand")
    void linkedCreatureDeathExilesCreatureAndReturnsServitude() {
        Card bears = castServitudeAndReturnBears();
        Permanent reanimated = findPermanentByCardId(bears.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, reanimated.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanentByCardId(bears.getId())).isNull();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bears.getId())
                && entry.ownerId().equals(player1.getId()));
        harness.assertInHand(player1, "Diabolic Servitude");
    }

    @Test
    @DisplayName("When the enchantment leaves, the linked creature is exiled")
    void leavingExilesLinkedCreature() {
        Card bears = castServitudeAndReturnBears();
        Permanent servitude = findPermanent(player1, "Diabolic Servitude");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, servitude));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanentByCardId(bears.getId())).isNull();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bears.getId())
                && entry.ownerId().equals(player1.getId()));
        harness.assertInGraveyard(player1, "Diabolic Servitude");
    }

    @Test
    @DisplayName("The leave trigger exiles the linked card if it dies before that trigger resolves")
    void leavingExilesLinkedCardFromGraveyard() {
        Card bears = castServitudeAndReturnBears();
        Permanent servitude = findPermanent(player1, "Diabolic Servitude");
        Permanent reanimated = findPermanentByCardId(bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, servitude));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, reanimated.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bears.getId())
                && entry.ownerId().equals(player1.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A different creature dying does not break the link")
    void unrelatedCreatureDeathDoesNothing() {
        Card bears = castServitudeAndReturnBears();
        Card unrelatedCard = new GrizzlyBears();
        harness.addToBattlefield(player1, unrelatedCard);
        Permanent unrelated = findPermanentByCardId(unrelatedCard.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, unrelated.getId());
        harness.passBothPriorities();

        assertThat(findPermanentByCardId(bears.getId())).isNotNull();
        harness.assertOnBattlefield(player1, "Diabolic Servitude");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Card castServitudeAndReturnBears() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new DiabolicServitude()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        return bears;
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
