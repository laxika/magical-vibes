package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SisterOfSilence.class, Shock.class, IcyManipulator.class, GrizzlyBears.class})
class SisterOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant spell when it enters")
    void countersInstantSpellOnEntry() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SisterOfSilence()));
        addSisterMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Sister of Silence");
    }

    @Test
    @DisplayName("Counters an activated ability when it enters")
    void countersActivatedAbilityOnEntry() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        IcyManipulator icy = new IcyManipulator();
        harness.addToBattlefield(player2, icy);
        Permanent icyPermanent = findPermanent(player2, "Icy Manipulator");
        icyPermanent.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, null, bearsId);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SisterOfSilence()));
        addSisterMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, icy.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when only an illegal creature spell is on the stack")
    void doesNotTriggerForCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new SisterOfSilence()));
        addSisterMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(bears);
    }

    private void addSisterMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
