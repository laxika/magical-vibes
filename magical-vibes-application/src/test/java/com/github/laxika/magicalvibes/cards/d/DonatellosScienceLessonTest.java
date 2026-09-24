package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({DonatellosScienceLesson.class, GrizzlyBears.class, Island.class})
class DonatellosScienceLessonTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two creatures and makes up to two players draw")
    void tapsCreaturesAndDrawsForPlayers() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        stockLibraries(2);
        prepareCard();
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0,
                List.of(first.getId(), second.getId(), player1.getId(), player2.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize + 1);
    }

    @Test
    @DisplayName("Can choose only player targets")
    void canChooseOnlyPlayers() {
        stockLibraries(1);
        prepareCard();
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize - 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize + 1);
    }

    @Test
    void cannotChooseMoreThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature permanent as a creature target")
    void rejectsNoncreatureTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeCastWithoutTargets() {
        prepareCard();

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new DonatellosScienceLesson()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void stockLibraries(int cardsPerPlayer) {
        harness.setLibrary(player1, cards(cardsPerPlayer));
        harness.setLibrary(player2, cards(cardsPerPlayer));
    }

    private List<Card> cards(int count) {
        return java.util.stream.Stream.<Card>generate(GrizzlyBears::new)
                .limit(count)
                .toList();
    }
}
