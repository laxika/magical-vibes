package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GiftOfImmortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Gift returns the creature immediately and reattaches at the next end step")
    void returnsCreatureAndReattachesAtNextEndStep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card creatureCard = creature.getCard();
        GiftOfImmortality giftCard = new GiftOfImmortality();

        castGift(creature, giftCard);
        killCreature(creature.getId());

        Permanent returnedCreature = findPermanent(player1, creatureCard.getId());
        assertThat(returnedCreature).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(giftCard.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returnedGift = findPermanent(player1, giftCard.getId());
        assertThat(returnedGift).isNotNull();
        assertThat(returnedGift.getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("Gift stays in the graveyard if the returned creature leaves before the end step")
    void doesNotReattachAfterReturnedCreatureLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card creatureCard = creature.getCard();
        GiftOfImmortality giftCard = new GiftOfImmortality();

        castGift(creature, giftCard);
        killCreature(creature.getId());
        Permanent returnedCreature = findPermanent(player1, creatureCard.getId());
        assertThat(returnedCreature).isNotNull();

        killCreature(returnedCreature.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, giftCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(giftCard.getId()));
    }

    private void castGift(Permanent creature, GiftOfImmortality giftCard) {
        harness.setHand(player1, List.of(giftCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void killCreature(UUID creatureId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creatureId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Player player, UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
