package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AttendedKnight.class})
class AttendedKnightTest extends BaseCardTest {

    @Test
    @DisplayName("When Attended Knight enters the battlefield, a Soldier token is created")
    void etbCreatesSoldierToken() {
        harness.castFromHand(player1, new AttendedKnight(), "{2}{W}");
        harness.passBothPriorities(); // Resolve creature — ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        harness.assertOnBattlefield(player1, "Attended Knight");
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("ETB token is a 1/1 white Soldier creature token")
    void tokenIsWhiteSoldier() {
        harness.castFromHand(player1, new AttendedKnight(), "{2}{W}");
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        Permanent token = findPermanent(player1, "Soldier");

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Soldier is created only when the enter trigger resolves")
    void tokenCreationUsesTheStack() {
        harness.castFromHand(player1, new AttendedKnight(), "{2}{W}");

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Attended Knight");
        assertThat(findPermanents(player1, "Soldier")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        assertThat(findPermanent(player1, "Soldier").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast creates a Soldier for the entering controller")
    void enteringWithoutCastingCreatesToken() {
        harness.enterBattlefieldAndReturn(player2, new AttendedKnight());

        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Soldier")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }
}
