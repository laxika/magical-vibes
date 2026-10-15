package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZiatoraTheIncinerator.class, GrizzlyBears.class, Murder.class})
class ZiatoraTheIncineratorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature deals its power as damage and creates three Treasures")
    void sacrificeDealsPowerDamageAndCreatesTreasures() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Declining the sacrifice creates no Treasures and deals no damage")
    void decliningSacrificeDoesNothing() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Having no other creature to sacrifice creates no Treasures")
    void noOtherCreatureDoesNothing() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("A zero-power sacrifice still creates three Treasures")
    void zeroPowerStillCreatesTreasures() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        fodder.setPowerModifier(-2);
        int lifeBefore = gd.getLife(player2.getId());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Damage uses the sacrificed creature's modified power")
    void usesModifiedPowerAtSacrifice() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        fodder.setPowerModifier(3);
        int lifeBefore = gd.getLife(player2.getId());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 5);
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Removing the reflexive trigger's target prevents Treasure creation")
    void illegalTargetPreventsTreasures() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Ziatora does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new ZiatoraTheIncinerator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void resolveEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(activePlayer.getId());
    }
}
