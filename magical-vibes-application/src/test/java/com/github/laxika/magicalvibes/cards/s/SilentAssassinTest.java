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

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
    }
}
