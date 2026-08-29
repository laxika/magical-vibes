package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({PartingGust.class, GrizzlyBears.class, Plains.class})
class PartingGustTest extends BaseCardTest {

    @Test
    @DisplayName("Without the gift, the creature returns at the next end step with a counter")
    void withoutGiftReturnsCreatureWithCounter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(bear.getId(), false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(bear.getCard().getId());

        advanceToEndStep();

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bear.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(bear.getCard().getId()));
    }

    @Test
    @DisplayName("Promising the gift exiles the creature and gives the opponent a tapped Fish")
    void giftExilesCreatureAndCreatesTappedFish() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(bear.getId(), true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).contains(bear.getCard().getId());

        advanceToEndStep();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
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

    private void cast(UUID targetId, boolean giftPromised) {
        harness.setHand(player1, List.of(new PartingGust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstantWithGift(player1, 0, targetId, giftPromised);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
