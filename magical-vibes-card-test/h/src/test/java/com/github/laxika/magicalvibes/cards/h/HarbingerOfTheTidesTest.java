package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Omniscience;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarbingerOfTheTides.class, GrizzlyBears.class, Omniscience.class})
class HarbingerOfTheTidesTest extends BaseCardTest {

    private Permanent tappedBears(Player controller) {
        Permanent bears = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        bears.tap();
        return bears;
    }

    private void hardcast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarbingerOfTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns the chosen tapped opponent creature to its owner's hand")
    void etbBouncesTappedOpponentCreature() {
        UUID bearsId = tappedBears(player2).getId();
        hardcast();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Harbinger of the Tides");
    }

    @Test
    @DisplayName("Declining the may leaves the tapped creature on the battlefield")
    void decliningMayLeavesCreature() {
        UUID bearsId = tappedBears(player2).getId();
        hardcast();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Harbinger of the Tides");
    }

    @Test
    @DisplayName("An untapped opponent creature is not a legal target, so no trigger is offered")
    void untappedOpponentCreatureIsNotTargetable() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarbingerOfTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Harbinger of the Tides");
    }

    @Test
    @DisplayName("A tapped creature you control is not a legal target")
    void ownTappedCreatureIsNotTargetable() {
        tappedBears(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarbingerOfTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast on an opponent's turn by paying {2} more")
    void flashCastForTwoMore() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarbingerOfTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Harbinger of the Tides");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot be hardcast on an opponent's turn")
    void noFlashWithoutTheSurcharge() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarbingerOfTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Harbinger of the Tides");
    }

    @Test
    @DisplayName("Normal sorcery timing requires only the printed mana cost")
    void normalCastCostsOnlyTwoBlue() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarbingerOfTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Harbinger of the Tides");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash casting cannot skip the two-mana surcharge")
    void flashCastRequiresFullPayment() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarbingerOfTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithEvoke(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Harbinger of the Tides");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The bounce does not resolve if its target becomes untapped")
    void untappedTargetIsIllegalAtResolution() {
        Permanent bears = tappedBears(player2);
        hardcast();
        harness.handlePermanentChosen(player1, bears.getId());

        bears.untap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Omniscience waives the mana cost but not the flash surcharge")
    void flashSurchargeCombinesWithOmniscience() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HarbingerOfTheTides()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Harbinger of the Tides");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
