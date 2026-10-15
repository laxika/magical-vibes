package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PartingGust.class, BarkformHarvester.class, Plains.class})
class PartingGustTest extends BaseCardTest {

    @Test
    @DisplayName("Without the gift, the creature returns at the next end step with a counter")
    void withoutGiftReturnsCreatureWithCounter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        cast(bear.getId(), false);

        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(bear.getCard().getId());

        advanceToEndStep();

        Permanent returned = findPermanent(player2, "Barkform Harvester");
        assertThat(returned.getId()).isNotEqualTo(bear.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(bear.getCard().getId()));
    }

    @Test
    @DisplayName("Promising the gift exiles the creature and gives the opponent a tapped Fish")
    void giftExilesCreatureAndCreatesTappedFish() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        cast(bear.getId(), true);

        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(bear.getCard().getId());

        advanceToEndStep();

        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(bear.getCard().getId());
    }

    @Test
    @DisplayName("Can target only a nontoken creature")
    void cannotTargetNoncreature() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new PartingGust()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, plains.getId(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken creature");
    }

    @Test
    @DisplayName("A Fish token is not a legal target even when another gift is promised")
    void cannotTargetToken() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        cast(creature.getId(), true);
        Permanent fish = findPermanent(player2, "Fish");
        harness.setHand(player1, List.of(new PartingGust()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, fish.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken creature");
        assertThat(countPermanents(player2, "Fish")).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target prevents the promised Fish from being created")
    void missingTargetDoesNotGiveGift() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.setHand(player1, List.of(new PartingGust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstantWithGift(player1, 0, creature.getId(), true);
        harness.getPermanentRemovalService().removePermanentToHand(gd, creature);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Barkform Harvester");
        harness.assertNotOnBattlefield(player2, "Fish");
        harness.assertInGraveyard(player1, "Parting Gust");
    }

    @Test
    @DisplayName("A creature controlled by a different player returns to its owner")
    void returnsToOwnerRatherThanPreviousController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        cast(creature.getId(), false);
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Barkform Harvester");
        Permanent returned = findPermanent(player2, "Barkform Harvester");
        assertThat(returned.getCard().getId()).isEqualTo(creature.getCard().getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting during an end step waits for the following turn's end step")
    void castDuringEndStepWaitsForNextEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        harness.forceStep(TurnStep.END_STEP);
        cast(creature.getId(), false);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Barkform Harvester");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Barkform Harvester");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void cast(UUID targetId, boolean giftPromised) {
        harness.setHand(player1, List.of(new PartingGust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstantWithGift(player1, 0, targetId, giftPromised);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
