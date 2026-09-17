package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JundCharm;
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

@CardUsed({CaseyJonesBackAlleyBrute.class, BurstOfStrength.class, JundCharm.class, GrizzlyBears.class})
class CaseyJonesBackAlleyBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a +1/+1 counter on a creature deals that much damage to an opponent")
    void counterPlacementDamagesOpponent() {
        harness.addToBattlefield(player1, new CaseyJonesBackAlleyBrute());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

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

    private PendingInteraction.PermanentChoice activePermanentChoice() {
        return (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
    }
}
