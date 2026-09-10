package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InscriptionOfRuin.class, GrizzlyBears.class})
class InscriptionOfRuinTest extends BaseCardTest {

    @Test
    void opponentDiscardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()), List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void returnsTargetCreatureWithManaValueTwoOrLess() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void destroysTargetCreatureWithManaValueThreeOrLess() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void kickerAllowsChoosingAllThreeModesInOrder() {
        Card reanimated = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(reanimated));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InscriptionOfRuin()));
        addMana(6);

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(player2.getId(), reanimated.getId(), destroyed.getId()), List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(reanimated.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destroyed);
    }

    @Test
    void cannotChooseMultipleModesWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0, 2}, List.of(player2.getId(), target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new InscriptionOfRuin()));
        addMana(3);
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.BLACK, amount);
    }
}
