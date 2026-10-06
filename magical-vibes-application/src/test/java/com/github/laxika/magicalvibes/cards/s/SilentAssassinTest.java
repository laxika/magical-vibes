package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilentAssassin.class, FreshVolunteers.class})
class SilentAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a blocking creature at end of combat")
    void destroysBlockingCreatureAtEndOfCombat() {
        addCreatureReady(player1, new SilentAssassin());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        blocker.setBlocking(true);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fresh Volunteers");

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addCreatureReady(player1, new SilentAssassin());
        Permanent bystander = addCreatureReady(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    @Test
    @DisplayName("Can activate more than once without tapping")
    void canActivateMoreThanOnceWithoutTapping() {
        Permanent assassin = addCreatureReady(player1, new SilentAssassin());
        Permanent firstBlocker = addCreatureReady(player2, new FreshVolunteers());
        Permanent secondBlocker = addCreatureReady(player2, new FreshVolunteers());
        firstBlocker.setBlocking(true);
        secondBlocker.setBlocking(true);
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, firstBlocker.getId());
        harness.activateAbility(player1, 0, null, secondBlocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(assassin.isTapped()).isFalse();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Fresh Volunteers"))
                .hasSize(2);
    }

    @Test
    @DisplayName("The target must still be blocking when the ability resolves")
    void targetMustStillBeBlockingOnResolution() {
        addCreatureReady(player1, new SilentAssassin());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        blocker.setBlocking(true);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Delayed destruction belongs to Silent Assassin and its ability controller")
    void delayedDestructionKeepsSourceAndController() {
        Permanent assassin = addCreatureReady(player1, new SilentAssassin());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        blocker.setBlocking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getLast().getSourcePermanentId()).isEqualTo(assassin.getId());

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Activation during end of combat waits for the next end of combat")
    void activationDuringEndOfCombatWaitsForNextCombat() {
        addCreatureReady(player1, new SilentAssassin());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        blocker.setBlocking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Removing the creature from combat after resolution does not cancel destruction")
    void destructionPersistsAfterCreatureStopsBlocking() {
        addCreatureReady(player1, new SilentAssassin());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        blocker.setBlocking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        blocker.setBlocking(false);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }
}
