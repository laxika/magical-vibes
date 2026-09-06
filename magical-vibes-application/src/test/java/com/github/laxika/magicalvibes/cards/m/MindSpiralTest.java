package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindSpiral.class, GrizzlyBears.class})
class MindSpiralTest extends BaseCardTest {

    @Test
    void withoutGiftTargetPlayerDrawsThreeCards() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player2.getId()).size();

        cast(List.of(player2.getId()), false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 3);
        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
        harness.assertNotOnBattlefield(player2, "Fish");
    }

    @Test
    void promisedGiftCreatesTappedFishAndTapsAndStunsTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player2.getId()).size();

        cast(List.of(player2.getId(), bear.getId()), true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 3);
        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish).isNotNull();
        assertThat(fish.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void withoutGiftCannotChooseGiftOnlyTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, List.of(player2.getId(), bear.getId()), false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void promisedGiftRequiresCreatureTarget() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, List.of(player2.getId()), true))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds, boolean giftPromised) {
        prepareSpell();
        harness.castSorceryWithGift(player1, 0, targetIds, giftPromised);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new MindSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
