package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GathanRaiders.class, Forest.class})
class GathanRaidersTest extends BaseCardTest {

    @Test
    void getsPlusTwoPlusTwoWithAnEmptyHand() {
        harness.setHand(player1, List.of());
        Permanent raiders = harness.addToBattlefieldAndReturn(player1, new GathanRaiders());

        assertThat(gqs.getEffectivePower(gd, raiders)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, raiders)).isEqualTo(5);
    }

    @Test
    void losesHellbentBoostWhenItsControllerDrawsACard() {
        harness.setHand(player1, List.of());
        Permanent raiders = harness.addToBattlefieldAndReturn(player1, new GathanRaiders());

        harness.setHand(player1, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, raiders)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raiders)).isEqualTo(3);
    }

    @Test
    void morphsForThreeAndTurnsFaceUpByDiscardingACard() {
        Forest discarded = new Forest();
        harness.setHand(player1, List.of(new GathanRaiders(), discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent raiders = findPermanent(player1, "Gathan Raiders");
        assertThat(raiders.isFaceDown()).isTrue();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raiders), 0);
        harness.passBothPriorities();

        assertThat(raiders.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.getEffectivePower(gd, raiders)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, raiders)).isEqualTo(5);
    }

    @Test
    void cannotTurnFaceUpWithoutACardToDiscard() {
        harness.setHand(player1, List.of(new GathanRaiders()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent raiders = findPermanent(player1, "Gathan Raiders");
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(raiders)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(raiders.isFaceDown()).isTrue();
    }
}
