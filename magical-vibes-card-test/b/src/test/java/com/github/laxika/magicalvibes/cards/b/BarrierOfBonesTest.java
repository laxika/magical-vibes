package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrierOfBones.class})
class BarrierOfBonesTest extends BaseCardTest {

    @Test
    void entersWithSurveilOne() {
        Card topCard = new BarrierOfBones();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new BarrierOfBones()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void surveilCanLeaveTheCardOnTop() {
        Card topCard = new BarrierOfBones();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new BarrierOfBones()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void surveilOnlyMovesTheTopCardOfItsControllersLibrary() {
        Card topCard = new BarrierOfBones();
        Card secondCard = new BarrierOfBones();
        Card opponentCard = new BarrierOfBones();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new BarrierOfBones()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void surveilWithAnEmptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BarrierOfBones()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Barrier of Bones");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void defenderPreventsAttacking() {
        Permanent barrier = addCreatureReady(player1, new BarrierOfBones());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(barrier.isAttacking()).isFalse();
    }
}
