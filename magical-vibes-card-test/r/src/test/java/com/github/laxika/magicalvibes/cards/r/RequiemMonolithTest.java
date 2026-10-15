package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RequiemMonolith.class, GrizzlyBears.class, Shock.class})
class RequiemMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("The target creature's controller may have the Monolith deal damage and then draws and loses that much")
    void targetControllerAcceptsDamage() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted ability triggers for lethal damage and uses the total damage dealt")
    void lethalDamageTriggersDrawAndLifeLoss() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void decliningDamageDoesNotDrawOrLoseLife() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void canTargetOwnCreatureAndControllerChooses() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void grantedAbilityExpiresAfterTurn() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void combatDamageAlsoTriggersGrantedAbility() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void multipleGrantsTriggerSeparatelyForSameDamage() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void removedTargetDoesNotOfferDamageChoice() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotActivateTappedMonolith() {
        Permanent monolith = harness.addToBattlefieldAndReturn(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        monolith.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
