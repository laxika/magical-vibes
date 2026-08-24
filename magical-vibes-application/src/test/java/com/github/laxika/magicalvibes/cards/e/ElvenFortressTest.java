package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.v.VodalianWarMachine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvenFortress.class, VodalianWarMachine.class})
class ElvenFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature +0/+1 until end of turn")
    void boostsBlockingCreatureUntilEndOfTurn() {
        addFortress();
        Permanent blocker = addBlockingCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(0);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(0);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addFortress();
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it is marked blocking")
    void cannotTargetBlockingNoncreature() {
        addFortress();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new ElvenFortress());
        noncreature.setBlocking(true);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    @Test
    @DisplayName("Target must still be blocking when the ability resolves")
    void fizzlesWhenTargetStopsBlocking() {
        addFortress();
        Permanent blocker = addBlockingCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(0);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    private void addFortress() {
        harness.addToBattlefield(player1, new ElvenFortress());
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new VodalianWarMachine());
    }

    private Permanent addBlockingCreature(Player player) {
        Permanent blocker = addCreature(player);
        blocker.setBlocking(true);
        return blocker;
    }
}
