package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sanctimony.class, Mountain.class, Forest.class, Twiddle.class})
class SanctimonyTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent tapping a Mountain for mana gains the controller 1 life")
    void opponentTapsMountainGainsLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller tapping their own Mountain does not gain life (only opponents)")
    void controllerTapsMountainNoLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Mountain is at index 1 (Sanctimony at index 0)
        harness.tapPermanent(player1, 1);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent tapping a non-Mountain land does not gain life")
    void opponentTapsNonMountainNoLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each opponent Mountain tap triggers Sanctimony separately")
    void multipleMountainTapsGainMultipleLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Tapping an opponent's Mountain without producing mana does not trigger Sanctimony")
    void opponentTapsMountainWithoutProducingManaNoLife() {
        harness.addToBattlefield(player1, new Sanctimony());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player2, List.of(new Twiddle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, mountain.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(mountain.isTapped()).isTrue();
        harness.assertLife(player1, 20);
    }
}
