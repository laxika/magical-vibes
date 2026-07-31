package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KrovikanHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers at end step with a creature card directly above it")
    void triggersWithCreatureDirectlyAbove() {
        KrovikanHorror horror = new KrovikanHorror();
        harness.setGraveyard(player1, List.of(horror, new GrizzlyBears()));

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(horror.getId());
    }

    @Test
    @DisplayName("Accepting returns it from the graveyard to its owner's hand")
    void acceptReturnsToHand() {
        KrovikanHorror horror = new KrovikanHorror();
        harness.setGraveyard(player1, List.of(horror, new GrizzlyBears()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(horror.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(horror.getId()));
    }

    @Test
    @DisplayName("Declining keeps it in the graveyard")
    void declineKeepsInGraveyard() {
        KrovikanHorror horror = new KrovikanHorror();
        harness.setGraveyard(player1, List.of(horror, new GrizzlyBears()));

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(horror.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(horror.getId()));
    }

    @Test
    @DisplayName("Does not trigger when the card directly above is not a creature")
    void doesNotTriggerWithNoncreatureDirectlyAbove() {
        harness.setGraveyard(player1, List.of(new KrovikanHorror(), new Shock(), new GrizzlyBears()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when it is the top card of the graveyard")
    void doesNotTriggerWhenOnTop() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new KrovikanHorror()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers on an opponent's end step too")
    void triggersOnOpponentEndStep() {
        KrovikanHorror horror = new KrovikanHorror();
        harness.setGraveyard(player1, List.of(horror, new GrizzlyBears()));

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(horror.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Activated ability sacrifices a creature and deals 1 damage to a player")
    void abilityDealsOneDamageToPlayer() {
        addCreatureReady(player1, new KrovikanHorror());
        addCreatureReady(player1, new GrizzlyBears());
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, bears);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Krovikan Horror");
    }

    @Test
    @DisplayName("Activated ability can kill a 1-toughness creature")
    void abilityDamagesCreature() {
        addCreatureReady(player1, new KrovikanHorror());
        addCreatureReady(player1, new GrizzlyBears());
        UUID fodder = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new FugitiveWizard());
        UUID victim = harness.getPermanentId(player2, "Fugitive Wizard");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, victim);
        harness.handlePermanentChosen(player1, fodder);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
