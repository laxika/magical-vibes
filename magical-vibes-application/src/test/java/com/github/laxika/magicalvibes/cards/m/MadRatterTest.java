package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadRatter.class, RovingKeep.class})
class MadRatterTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates two Rat tokens")
    void secondDrawCreatesTwoRatTokens() {
        harness.addToBattlefieldAndReturn(player1, new MadRatter());
        addCardsToDeck(2);

        draw();
        assertThat(gd.stack).isEmpty();

        draw();
        resolveTopOfStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger does not fire on later draws in the same turn")
    void triggersOnlyOnSecondDraw() {
        Permanent ratter = harness.addToBattlefieldAndReturn(player1, new MadRatter());
        addCardsToDeck(3);

        draw();
        draw();
        resolveTopOfStack();
        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(ratter.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("The controller can trigger Mad Ratter on an opponent's turn")
    void triggersOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new MadRatter());
        addCardsToDeck(2);

        draw();
        draw();
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Rat")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.RAT);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(findPermanents(player2, "Rat")).isEmpty();
    }

    @Test
    @DisplayName("An opponent drawing two cards does not trigger Mad Ratter")
    void opponentsDrawsDoNotTrigger() {
        harness.addToBattlefield(player1, new MadRatter());
        harness.setLibrary(player2, IntStream.range(0, 2)
                .mapToObj(i -> new RovingKeep()).toList());

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    @DisplayName("A first draw before Mad Ratter enters still counts")
    void firstDrawBeforeEnteringCounts() {
        addCardsToDeck(2);
        draw();
        harness.addToBattlefield(player1, new MadRatter());

        draw();
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Rat")).hasSize(2);
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on the third")
    void enteringAfterSecondDrawDoesNotTrigger() {
        addCardsToDeck(3);
        draw();
        draw();
        harness.addToBattlefield(player1, new MadRatter());

        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    private void addCardsToDeck(int count) {
        harness.setLibrary(player1, IntStream.range(0, count)
                .mapToObj(i -> new RovingKeep()).toList());
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
