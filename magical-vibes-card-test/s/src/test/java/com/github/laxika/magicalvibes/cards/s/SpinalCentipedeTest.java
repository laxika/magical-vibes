package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinalCentipede.class, GrizzlyBears.class, Shock.class, BartizanBats.class, DeadWeight.class})
class SpinalCentipedeTest extends BaseCardTest {

    @Test
    @DisplayName("When Spinal Centipede dies, it puts a +1/+1 counter on a creature you control")
    void deathTriggerPutsCounterOnCreatureYouControl() {
        Permanent centipede = addCreatureReady(player1, new SpinalCentipede());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, centipede.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Spinal Centipede cannot target an opponent's creature with its death trigger")
    void deathTriggerCannotTargetOpponentCreature() {
        Permanent centipede = addCreatureReady(player1, new SpinalCentipede());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, centipede.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Spinal Centipede");
    }

    @Test
    @DisplayName("Death trigger does nothing when no creatures remain")
    void deathTriggerWithNoRemainingCreatures() {
        Permanent centipede = harness.addToBattlefieldAndReturn(player1, new SpinalCentipede());
        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, centipede.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spinal Centipede");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger cannot put a counter on a target that dies before resolution")
    void deathTriggerDoesNotRetargetWhenTargetDies() {
        Permanent centipede = harness.addToBattlefieldAndReturn(player1, new SpinalCentipede());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BartizanBats());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BartizanBats());
        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, centipede.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new DeadWeight());
        aura.setAttachedTo(target.getId());
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
