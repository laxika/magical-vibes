package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilversmoteGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Returns tapped from the graveyard at your end step after gaining 3 life")
    void returnsTappedAtOwnEndStepAfterGainingThreeLife() {
        SilversmoteGhoul ghoul = new SilversmoteGhoul();
        harness.setGraveyard(player1, List.of(ghoul));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(ghoul.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ghoul.getId()) && permanent.isTapped());
    }

    @Test
    @DisplayName("Does not return at an opponent's end step")
    void doesNotReturnAtOpponentsEndStep() {
        SilversmoteGhoul ghoul = new SilversmoteGhoul();
        harness.setGraveyard(player1, List.of(ghoul));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ghoul);
    }

    @Test
    @DisplayName("Does not return without gaining at least 3 life")
    void doesNotReturnBelowLifeThreshold() {
        SilversmoteGhoul ghoul = new SilversmoteGhoul();
        harness.setGraveyard(player1, List.of(ghoul));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ghoul);
    }

    @Test
    @DisplayName("Sacrifices itself and draws a card")
    void sacrificesAndDraws() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new SilversmoteGhoul());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silversmote Ghoul");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
