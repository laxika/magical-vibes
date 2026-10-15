package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DurableCoilbug.class})
class DurableCoilbugTest extends BaseCardTest {

    @Test
    void graveyardAbilityGoesOnStackAndPaysMana() {
        harness.setGraveyard(player1, List.of(new DurableCoilbug()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void resolvingGraveyardAbilityReturnsCardToHand() {
        harness.setGraveyard(player1, List.of(new DurableCoilbug()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Durable Coilbug");
        harness.assertNotInGraveyard(player1, "Durable Coilbug");
    }

    @Test
    void cannotActivateGraveyardAbilityWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new DurableCoilbug()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Durable Coilbug");
    }

    @Test
    void returnsOnlyTheActivatingCopy() {
        DurableCoilbug otherCopy = new DurableCoilbug();
        DurableCoilbug activatingCopy = new DurableCoilbug();
        DurableCoilbug opponentsCopy = new DurableCoilbug();
        harness.setGraveyard(player1, List.of(otherCopy, activatingCopy));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(activatingCopy).doesNotContain(otherCopy);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
    }

    @Test
    void multipleActivationsReturnTheCardOnlyOnce() {
        DurableCoilbug coilbug = new DurableCoilbug();
        harness.setGraveyard(player1, List.of(coilbug));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(coilbug.getId())).count()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Durable Coilbug");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayTheBlackRequirementWithColorlessMana() {
        harness.setGraveyard(player1, List.of(new DurableCoilbug()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Durable Coilbug");
    }

    @Test
    void canActivateDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.setGraveyard(player1, List.of(new DurableCoilbug()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Durable Coilbug");
        harness.assertNotInGraveyard(player1, "Durable Coilbug");
    }
}
