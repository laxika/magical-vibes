package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenVortex.class, Mountain.class, GrizzlyBears.class})
class MoltenVortexTest extends BaseCardTest {

    private void setUpMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new MoltenVortex());
    }

    @Test
    @DisplayName("Paying {R} and discarding a land deals 2 damage to a player")
    void dealsDamageToPlayer() {
        setUpMainPhase();
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("The ability deals 2 damage to a creature, killing a 2/2")
    void dealsDamageToCreature() {
        setUpMainPhase();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the ability without a land card in hand")
    void cannotActivateWithoutLand() {
        setUpMainPhase();
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutMana() {
        setUpMainPhase();
        harness.setHand(player1, new ArrayList<>(List.of(new Mountain())));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The land is discarded before the ability resolves")
    void paysDiscardCostBeforeResolution() {
        setUpMainPhase();
        harness.setHand(player1, List.of(new Mountain(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotInHand(player1, "Mountain");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, startingLife);

        harness.passBothPriorities();
        harness.assertLife(player2, startingLife - 2);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly without tapping")
    void canActivateTwiceInSameTurn() {
        setUpMainPhase();
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        harness.assertLife(player2, startingLife - 4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller is a legal target")
    void canDamageController() {
        setUpMainPhase();
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife - 2);
    }

    @Test
    @DisplayName("A noncreature enchantment is not a legal damage target")
    void cannotTargetNoncreatureEnchantment() {
        setUpMainPhase();
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent vortex = findPermanent(player1, "Molten Vortex");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, vortex.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
