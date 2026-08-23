package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidmageHusher.class, IcyManipulator.class, GrizzlyBears.class, Shock.class})
class VoidmageHusherTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target activated ability when it enters")
    void countersActivatedAbilityOnEntry() {
        IcyManipulator icy = new IcyManipulator();
        harness.addToBattlefield(player2, icy);
        findPermanent(player2, "Icy Manipulator").setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, targetId);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new VoidmageHusher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        UUID activatedAbilityId = findPermanent(player2, "Icy Manipulator").getCard().getId();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, activatedAbilityId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Icy Manipulator").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Voidmage Husher");
    }

    @Test
    @DisplayName("May return itself to its owner's hand when its controller casts a spell")
    void mayReturnToHandWhenControllerCastsSpell() {
        harness.addToBattlefield(player1, new VoidmageHusher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Voidmage Husher");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Does not counter a spell with the enters-the-battlefield ability")
    void doesNotCounterSpell() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new VoidmageHusher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(shock);

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Shock");
    }
}
