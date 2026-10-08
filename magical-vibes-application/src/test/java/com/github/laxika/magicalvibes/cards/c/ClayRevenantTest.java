package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClayRevenant.class})
class ClayRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped when cast")
    void entersTappedWhenCast() {
        harness.setHand(player1, List.of(new ClayRevenant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent revenant = findPermanent(player1, "Clay Revenant");
        assertThat(revenant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability returns it to hand")
    void returnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new ClayRevenant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Clay Revenant");
        harness.assertNotInGraveyard(player1, "Clay Revenant");
    }

    @Test
    @DisplayName("Returns only the source card among multiple copies")
    void returnsOnlyTheSourceCard() {
        ClayRevenant source = new ClayRevenant();
        ClayRevenant other = new ClayRevenant();
        ClayRevenant opponentsCopy = new ClayRevenant();
        harness.setGraveyard(player1, List.of(other, source));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, source);
        harness.assertNotInHand(player1, "Clay Revenant");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(source).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
    }

    @Test
    @DisplayName("Multiple activations return the source only once")
    void multipleActivationsReturnSourceOnlyOnce() {
        ClayRevenant source = new ClayRevenant();
        ClayRevenant other = new ClayRevenant();
        harness.setGraveyard(player1, List.of(source, other));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsOnlyOnce(source).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Graveyard ability requires black mana")
    void cannotActivateWithoutBlackMana() {
        ClayRevenant source = new ClayRevenant();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        harness.assertNotInHand(player1, "Clay Revenant");
    }
}
