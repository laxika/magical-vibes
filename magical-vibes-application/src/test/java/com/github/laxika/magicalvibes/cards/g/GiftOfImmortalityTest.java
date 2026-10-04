package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LashOfTheWhip;
import com.github.laxika.magicalvibes.cards.s.SedgeScorpion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiftOfImmortality.class, SedgeScorpion.class, LashOfTheWhip.class})
class GiftOfImmortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Gift returns the creature immediately and reattaches at the next end step")
    void returnsCreatureAndReattachesAtNextEndStep() {
        Permanent creature = addCreatureReady(player1, new SedgeScorpion());
        Card creatureCard = creature.getCard();
        GiftOfImmortality giftCard = new GiftOfImmortality();

        castGift(creature, giftCard);
        killCreature(creature.getId());

        Permanent returnedCreature = findPermanent(player1, creatureCard.getId());
        assertThat(returnedCreature).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(giftCard.getId()));

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent returnedGift = findPermanent(player1, giftCard.getId());
        assertThat(returnedGift).isNotNull();
        assertThat(returnedGift.getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("Gift stays in the graveyard if the returned creature leaves before the end step")
    void doesNotReattachAfterReturnedCreatureLeaves() {
        Permanent creature = addCreatureReady(player1, new SedgeScorpion());
        Card creatureCard = creature.getCard();
        GiftOfImmortality giftCard = new GiftOfImmortality();

        castGift(creature, giftCard);
        killCreature(creature.getId());
        Permanent returnedCreature = findPermanent(player1, creatureCard.getId());
        assertThat(returnedCreature).isNotNull();

        killCreature(returnedCreature.getId());

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, giftCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(giftCard.getId()));
    }

    @Test
    @DisplayName("The creature's owner gets the creature back while Gift returns to its own owner")
    void returnsOpponentsCreatureToItsOwner() {
        Permanent creature = addCreatureReady(player2, new SedgeScorpion());
        Card creatureCard = creature.getCard();
        GiftOfImmortality giftCard = new GiftOfImmortality();

        castGift(creature, giftCard);
        killCreature(creature.getId());

        Permanent returnedCreature = findPermanent(player2, creatureCard.getId());
        assertThat(returnedCreature).isNotNull();
        assertThat(findPermanent(player1, creatureCard.getId())).isNull();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent returnedGift = findPermanent(player1, giftCard.getId());
        assertThat(returnedGift).isNotNull();
        assertThat(returnedGift.getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("Players can kill the returned creature in response to the delayed return trigger")
    void canRespondToDelayedReturn() {
        Permanent creature = addCreatureReady(player1, new SedgeScorpion());
        Card creatureCard = creature.getCard();
        GiftOfImmortality giftCard = new GiftOfImmortality();

        castGift(creature, giftCard);
        killCreature(creature.getId());
        Permanent returnedCreature = findPermanent(player1, creatureCard.getId());
        assertThat(returnedCreature).isNotNull();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(findPermanent(player1, giftCard.getId())).isNull();
        assertThat(gd.stack).hasSize(1);
        killCreatureAtCurrentStep(returnedCreature.getId());

        assertThat(findPermanent(player1, giftCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(giftCard.getId()));
    }

    @Test
    @DisplayName("Death during an end step delays Gift until the following turn's end step")
    void deathDuringEndStepWaitsForNextEndStep() {
        Permanent creature = addCreatureReady(player1, new SedgeScorpion());
        Card creatureCard = creature.getCard();
        GiftOfImmortality giftCard = new GiftOfImmortality();

        castGift(creature, giftCard);
        harness.passUntil(TurnStep.END_STEP);
        killCreatureAtCurrentStep(creature.getId());

        Permanent returnedCreature = findPermanent(player1, creatureCard.getId());
        assertThat(returnedCreature).isNotNull();
        assertThat(findPermanent(player1, giftCard.getId())).isNull();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent returnedGift = findPermanent(player1, giftCard.getId());
        assertThat(returnedGift).isNotNull();
        assertThat(returnedGift.getAttachedTo()).isEqualTo(returnedCreature.getId());
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
        killCreatureAtCurrentStep(creatureId);
    }

    private void killCreatureAtCurrentStep(UUID creatureId) {
        harness.setHand(player1, List.of(new LashOfTheWhip()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, creatureId);
        resolveAllTriggers();
    }

    private Permanent findPermanent(Player player, UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
