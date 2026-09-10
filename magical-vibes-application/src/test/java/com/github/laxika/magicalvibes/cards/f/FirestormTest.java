package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LlanowarBehemoth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Firestorm.class, LlanowarBehemoth.class})
class FirestormTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 discards two cards and deals 2 damage to each of two targets")
    void dealsXDamageToEachOfXTargets() {
        harness.setHand(player1, new ArrayList<>(
                List.of(new Firestorm(), new LlanowarBehemoth(), new LlanowarBehemoth())));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(player1.getId(), player2.getId()), List.of(1, 2));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Behemoth", "Llanowar Behemoth", "Firestorm");
    }

    @Test
    @DisplayName("Each target takes the full X, not a divided share")
    void damageIsNotDivided() {
        var behemoth1 = harness.addToBattlefieldAndReturn(player2, new LlanowarBehemoth());
        var behemoth2 = harness.addToBattlefieldAndReturn(player2, new LlanowarBehemoth());
        harness.setHand(player1, new ArrayList<>(List.of(new Firestorm(),
                new LlanowarBehemoth(), new LlanowarBehemoth(), new LlanowarBehemoth(),
                new LlanowarBehemoth())));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForXWithDiscards(player1, 0, 4,
                List.of(behemoth1.getId(), behemoth2.getId(), player1.getId(), player2.getId()),
                List.of(1, 2, 3, 4));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Behemoth");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cast is rejected when the hand cannot cover X discards")
    void castRejectedWithoutEnoughCardsToDiscard() {
        harness.setHand(player1, new ArrayList<>(List.of(new Firestorm(), new LlanowarBehemoth())));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(player1.getId(), player2.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("More targets than X is rejected")
    void moreTargetsThanXIsRejected() {
        harness.setHand(player1, new ArrayList<>(List.of(new Firestorm(), new LlanowarBehemoth())));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player1, 0, 1,
                List.of(player1.getId(), player2.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fewer targets than X is rejected")
    void fewerTargetsThanXIsRejected() {
        harness.setHand(player1, new ArrayList<>(List.of(new Firestorm(),
                new LlanowarBehemoth(), new LlanowarBehemoth())));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(player1, 0, 2,
                List.of(player2.getId()), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X=0 requires no targets or discards")
    void zeroXRequiresNoTargetsOrDiscards() {
        harness.setHand(player1, List.of(new Firestorm()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForXWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Firestorm");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
