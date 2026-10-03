package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrackishTrudge.class})
class BrackishTrudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new BrackishTrudge(), "{2}{B}");
        harness.passBothPriorities();

        Permanent trudge = findPermanent(player1, "Brackish Trudge");
        assertThat(trudge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate its graveyard ability without life gain")
    void cannotActivateWithoutLifeGain() {
        harness.setGraveyard(player1, List.of(new BrackishTrudge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns itself from the graveyard after life gain")
    void returnsFromGraveyardAfterLifeGain() {
        BrackishTrudge trudge = new BrackishTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(trudge);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(trudge);
    }

    @Test
    @DisplayName("Opponent life gain does not permit activation")
    void opponentLifeGainDoesNotPermitActivation() {
        harness.setGraveyard(player1, List.of(new BrackishTrudge()));
        gd.lifeGainedThisTurn.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Brackish Trudge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns only the copy whose ability was activated")
    void returnsOnlyActivatedCopy() {
        BrackishTrudge first = new BrackishTrudge();
        BrackishTrudge second = new BrackishTrudge();
        BrackishTrudge opponentsCopy = new BrackishTrudge();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(second);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
    }

    @Test
    @DisplayName("Life gain does not waive the black mana requirement")
    void cannotActivateWithoutBlackMana() {
        harness.setGraveyard(player1, List.of(new BrackishTrudge()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Brackish Trudge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May activate twice but returns the card only once")
    void repeatedActivationReturnsCardOnlyOnce() {
        BrackishTrudge trudge = new BrackishTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(trudge.getId()))).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(trudge);
        assertThat(gd.stack).isEmpty();
    }
}
