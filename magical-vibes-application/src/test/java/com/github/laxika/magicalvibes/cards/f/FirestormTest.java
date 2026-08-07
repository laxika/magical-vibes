package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirestormTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 discards two cards and deals 2 damage to each of two targets")
    void dealsXDamageToEachOfXTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(
                List.of(new Firestorm(), new GiantGrowth(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(bears.getId(), player2.getId()), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Giant Growth", "Grizzly Bears", "Firestorm");
    }

    @Test
    @DisplayName("Each target takes the full X, not a divided share")
    void damageIsNotDivided() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Firestorm(),
                new GiantGrowth(), new GiantGrowth(), new GiantGrowth(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForXWithDiscards(player1, 0, 4,
                List.of(angel.getId(), bears.getId(), player2.getId()), List.of(1, 2, 3, 4));
        harness.passBothPriorities();

        // Each of the three targets takes the full 4, so the 4-toughness Serra Angel dies too.
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cast is rejected when the hand cannot cover X discards")
    void castRejectedWithoutEnoughCardsToDiscard() {
        harness.setHand(player1, new ArrayList<>(List.of(new Firestorm(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(player2.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("More targets than X is rejected")
    void moreTargetsThanXIsRejected() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Firestorm(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player1, 0, 1,
                List.of(bears.getId(), player2.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }
}
