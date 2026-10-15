package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavaSerpent.class})
class LavaSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {2} discards Lava Serpent and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new LavaSerpent()));
        LavaSerpent drawnCard = new LavaSerpent();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lava Serpent");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Cycling pays the discard immediately and draws only on resolution")
    void cyclingDiscardsBeforeDrawing() {
        LavaSerpent cycledCard = new LavaSerpent();
        LavaSerpent drawnCard = new LavaSerpent();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cycledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot discard the card without enough mana")
    void cyclingRequiresTwoMana() {
        LavaSerpent card = new LavaSerpent();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Lava Serpent");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling can be activated during the opponent's upkeep")
    void cyclingDuringOpponentsTurn() {
        LavaSerpent drawnCard = new LavaSerpent();
        harness.setHand(player1, List.of(new LavaSerpent()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lava Serpent");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lava Serpent can attack on the turn it is cast")
    void hasteAllowsImmediateAttack() {
        harness.castFromHand(player1, new LavaSerpent(), "{5}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(findPermanent(player1, "Lava Serpent").isTapped()).isTrue();
        harness.assertLife(player2, 15);
    }
}
