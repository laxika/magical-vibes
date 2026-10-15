package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MischievousMystic.class})
class MischievousMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a 1/1 blue Faerie token with flying")
    void secondDrawCreatesFaerieToken() {
        harness.addToBattlefieldAndReturn(player1, new MischievousMystic());
        addCardsToDeck(3);

        draw();
        assertThat(gd.stack).isEmpty();

        draw();
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FAERIE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger does not fire on later draws in the same turn")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new MischievousMystic());
        addCardsToDeck(3);

        draw();
        draw();
        resolveAllTriggers();
        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    void opponentsSecondDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new MischievousMystic());
        setLibrary(player2, 3);

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Faerie")).isZero();
    }

    @Test
    void secondDrawOnOpponentsTurnTriggers() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new MischievousMystic());
        addCardsToDeck(3);

        draw();
        draw();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Faerie")).isZero();
    }

    @Test
    void firstDrawBeforeMysticEntersStillCounts() {
        addCardsToDeck(3);
        draw();
        harness.addToBattlefield(player1, new MischievousMystic());

        draw();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
    }

    @Test
    void enteringAfterSecondDrawDoesNotTriggerRetroactively() {
        addCardsToDeck(4);
        draw();
        draw();
        harness.addToBattlefield(player1, new MischievousMystic());

        draw();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Faerie")).isZero();
    }

    @Test
    void secondDrawCanTriggerAgainOnNextTurn() {
        harness.addToBattlefield(player1, new MischievousMystic());
        addCardsToDeck(8);
        setLibrary(player2, 8);
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        draw();
        draw();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        draw();
        assertThat(gd.stack).isEmpty();
        draw();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(2);
    }

    private void addCardsToDeck(int count) {
        setLibrary(player1, count);
    }

    private void setLibrary(Player player, int count) {
        harness.setLibrary(player, IntStream.range(0, count)
                .mapToObj(i -> new MischievousMystic()).toList());
    }

    private void draw() {
        draw(player1);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
