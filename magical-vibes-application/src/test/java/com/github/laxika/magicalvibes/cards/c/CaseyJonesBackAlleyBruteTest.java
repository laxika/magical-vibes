package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JundCharm;
import com.github.laxika.magicalvibes.cards.l.LevelUp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseyJonesBackAlleyBrute.class, BurstOfStrength.class, JundCharm.class, GrizzlyBears.class,
        LevelUp.class})
class CaseyJonesBackAlleyBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a +1/+1 counter on a creature deals that much damage to an opponent")
    void counterPlacementDamagesOpponent() {
        harness.addToBattlefield(player1, new CaseyJonesBackAlleyBrute());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        PendingInteraction.PermanentChoice choice = activePermanentChoice();
        assertThat(choice.validPermanentIds()).isEmpty();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Two counters put on at once deal two damage in one trigger")
    void multipleCountersUseOneTriggerWithTheFullAmount() {
        harness.addToBattlefield(player1, new CaseyJonesBackAlleyBrute());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JundCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 2, bears.getId());
        harness.passBothPriorities();

        assertThat(activePermanentChoice().validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The attack trigger can target only an attacking creature")
    void attackTriggerTargetsAttackingCreature() {
        Permanent casey = addCreatureReady(player1, new CaseyJonesBackAlleyBrute());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        PendingInteraction.PermanentChoice choice = activePermanentChoice();
        assertThat(choice.validPermanentIds()).containsExactly(casey.getId());
        assertThat(choice.validIds()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, casey.getId());
        harness.passBothPriorities();

        assertThat(activePermanentChoice().validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(casey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Counters put by an opponent on your creature do not trigger Casey")
    void opponentPuttingCountersOnYourCreatureDoesNotTrigger() {
        Permanent casey = harness.addToBattlefieldAndReturn(player1, new CaseyJonesBackAlleyBrute());
        harness.setHand(player2, List.of(new LevelUp()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, casey.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(casey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Putting counters on an opponent's creature does not trigger your Casey")
    void puttingCountersOnOpponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new CaseyJonesBackAlleyBrute());
        Permanent opposingCasey = harness.addToBattlefieldAndReturn(player2, new CaseyJonesBackAlleyBrute());
        harness.setHand(player1, List.of(new LevelUp()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, opposingCasey.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opposingCasey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casey's attack trigger can put its counter on another attacking creature")
    void attackTriggerCanTargetAnotherAttacker() {
        Permanent casey = addCreatureReady(player1, new CaseyJonesBackAlleyBrute());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(activePermanentChoice().validPermanentIds())
                .containsExactlyInAnyOrder(casey.getId(), bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(activePermanentChoice().validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(casey.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 12);
    }

    private PendingInteraction.PermanentChoice activePermanentChoice() {
        return (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
    }
}
