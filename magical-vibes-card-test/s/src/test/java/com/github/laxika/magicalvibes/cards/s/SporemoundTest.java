package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sporemound.class, Forest.class})
class SporemoundTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall — playing a land creates a 1/1 Saproling token")
    void landfallCreatesSaproling() {
        harness.addToBattlefield(player1, new Sporemound());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCard().isToken()).isTrue();
        assertThat(saproling.getEffectivePower()).isEqualTo(1);
        assertThat(saproling.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent playing a land does not create a Saproling")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Sporemound());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("A land entering without being played creates exactly one Saproling")
    void landEnteringWithoutBeingPlayedTriggers() {
        harness.addToBattlefield(player1, new Sporemound());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCard().isToken()).isTrue();
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(saproling.isTapped()).isFalse();
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Each Sporemound triggers independently for the same land")
    void multipleSporemoundsEachCreateToken() {
        harness.addToBattlefield(player1, new Sporemound());
        harness.addToBattlefield(player1, new Sporemound());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("A nonland entering does not trigger landfall")
    void nonlandEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new Sporemound());

        harness.enterBattlefieldAndReturn(player1, new Sporemound());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }
}
