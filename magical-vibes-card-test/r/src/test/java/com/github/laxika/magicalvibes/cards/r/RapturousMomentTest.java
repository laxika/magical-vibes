package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RapturousMoment.class})
class RapturousMomentTest extends BaseCardTest {

    @Test
    @DisplayName("Mana is added only after both discard choices finish")
    void manaWaitsForDiscardChoices() {
        harness.setLibrary(player1, List.of(new RapturousMoment(), new RapturousMoment(),
                new RapturousMoment()));
        harness.castFromHand(player1, new RapturousMoment(), "{4}{U}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting Rapturous Moment puts it on the stack as a sorcery")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new RapturousMoment(), "{4}{U}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Resolving draws three, discards two, and adds UURRR")
    void resolvesDrawDiscardMana() {
        harness.castFromHand(player1, new RapturousMoment(), "{4}{U}{R}");
        harness.passBothPriorities();

        // Drew three cards (spell left hand), now choose two to discard.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Discard choices may include cards held before drawing")
    void canDiscardPreviouslyHeldCards() {
        harness.setHand(player2, List.of());
        RapturousMoment heldFirst = new RapturousMoment();
        RapturousMoment heldSecond = new RapturousMoment();
        RapturousMoment drawnFirst = new RapturousMoment();
        RapturousMoment drawnSecond = new RapturousMoment();
        RapturousMoment drawnThird = new RapturousMoment();
        harness.setHand(player1, List.of(new RapturousMoment(), heldFirst, heldSecond));
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond, drawnThird));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(drawnFirst, drawnSecond, drawnThird);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(heldFirst, heldSecond);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }
}
