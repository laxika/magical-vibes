package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClatteringAugur.class})
class ClatteringAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws a card and makes its controller lose 1 life")
    void enteringDrawsAndLosesLife() {
        harness.setHand(player1, List.of(new ClatteringAugur()));
        harness.setLibrary(player1, List.of(new ClatteringAugur()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Clattering Augur");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ClatteringAugur());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ClatteringAugur());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard ability returns this card to its owner's hand")
    void graveyardAbilityReturnsCardToHand() {
        Card augur = new ClatteringAugur();
        harness.setGraveyard(player1, List.of(augur));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Clattering Augur");
        harness.assertNotInGraveyard(player1, "Clattering Augur");
    }

    @Test
    @DisplayName("Graveyard ability returns only the copy whose ability was activated")
    void graveyardAbilityReturnsOnlyItsSource() {
        harness.setHand(player1, List.of());
        Card source = new ClatteringAugur();
        Card other = new ClatteringAugur();
        Card opponentsCopy = new ClatteringAugur();
        harness.setGraveyard(player1, List.of(other, source));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, source);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
    }

    @Test
    @DisplayName("Graveyard ability requires two black mana even with enough total mana")
    void graveyardAbilityRequiresTwoBlackMana() {
        Card augur = new ClatteringAugur();
        harness.setGraveyard(player1, List.of(augur));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(augur);
        assertThat(gd.stack).isEmpty();
    }

}
