package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FesteringNewt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BubblingCauldronTest extends BaseCardTest {

    private void setUpMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Sacrificing a creature gains 4 life")
    void sacrificeCreatureGainsFourLife() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate life-gain ability without a creature to sacrifice")
    void cannotGainLifeWithoutCreature() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a Festering Newt drains each opponent for 4 and gains that much life")
    void sacrificeNewtDrainsOpponent() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Festering Newt");
    }

    @Test
    @DisplayName("Cannot activate Newt ability by sacrificing a differently named creature")
    void cannotSacrificeNonNewtForDrain() {
        setUpMain();
        harness.addToBattlefield(player1, new BubblingCauldron());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
