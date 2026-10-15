package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlenElendraPranksters.class, Tarfire.class, WoodlandChangeling.class})
class GlenElendraPrankstersTest extends BaseCardTest {

    /** Puts player1 on defense during player2's turn so player1 may cast an instant. */
    private void enterOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting during an opponent's turn requires a target before the may choice")
    void triggersDuringOpponentTurn() {
        harness.addToBattlefield(player1, new GlenElendraPranksters());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting returns a chosen creature you control to hand")
    void acceptBouncesOwnCreature() {
        harness.addToBattlefield(player1, new GlenElendraPranksters());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling()).getId();

        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
        harness.assertInHand(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("Declining leaves the creature on the battlefield")
    void declineLeavesCreature() {
        harness.addToBattlefield(player1, new GlenElendraPranksters());
        harness.addToBattlefield(player1, new WoodlandChangeling());

        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Woodland Changeling"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("Casting on your own turn does not trigger")
    void doesNotTriggerOnOwnTurn() {
        harness.addToBattlefield(player1, new GlenElendraPranksters());
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("An opponent casting a spell does not trigger it")
    void doesNotTriggerForOpponentsSpell() {
        harness.addToBattlefield(player1, new GlenElendraPranksters());
        enterOpponentTurn();
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The may ability only offers creatures controlled by the Pranksters' controller")
    void onlyOffersCreaturesControllerControls() {
        UUID prankstersId = harness.addToBattlefieldAndReturn(player1, new GlenElendraPranksters()).getId();
        UUID ownBearsId = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling()).getId();
        UUID opponentBearsId = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling()).getId();

        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(ownBearsId, prankstersId)
                .doesNotContain(opponentBearsId);

        harness.handlePermanentChosen(player1, ownBearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Woodland Changeling");
        harness.assertOnBattlefield(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Pranksters can return itself before the spell that triggered it resolves")
    void canReturnItselfBeforeTriggeringSpellResolves() {
        UUID prankstersId = harness.addToBattlefieldAndReturn(player1, new GlenElendraPranksters()).getId();
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, prankstersId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Glen Elendra Pranksters");
        harness.assertNotOnBattlefield(player1, "Glen Elendra Pranksters");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A creature controlled by you returns to its owner's hand")
    void returnsBorrowedCreatureToOwnersHand() {
        harness.addToBattlefield(player1, new GlenElendraPranksters());
        WoodlandChangeling borrowed = new WoodlandChangeling();
        borrowed.setOwnerId(player2.getId());
        UUID borrowedId = harness.addToBattlefieldAndReturn(player1, borrowed).getId();
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, borrowedId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
        harness.assertInHand(player2, "Woodland Changeling");
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(borrowed.getId()));
    }
}
