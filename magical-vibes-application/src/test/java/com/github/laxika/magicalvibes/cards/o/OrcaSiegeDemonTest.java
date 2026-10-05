package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcaSiegeDemon.class, GrizzlyBears.class, Murder.class})
class OrcaSiegeDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature dying puts a +1/+1 counter on Orca")
    void anotherCreatureDiesPutsCounterOnOrca() {
        Permanent orca = addCreatureReady(player1, new OrcaSiegeDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithMurder(player1, bears.getId());

        assertThat(orca.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When Orca dies, it deals damage equal to its power")
    void deathDealsDamageEqualToPower() {
        Permanent orca = addCreatureReady(player1, new OrcaSiegeDemon());
        orca.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 6);

        destroyWithMurder(player1, orca.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("When Orca dies, its damage may be divided among targets")
    void deathDividesDamageAmongTargets() {
        Permanent orca = addCreatureReady(player1, new OrcaSiegeDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 3, player2.getId(), 2);

        destroyWithMurder(player1, orca.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(bears.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Orca's death asks for targets and damage division before players can respond")
    void deathRequestsTargetsAndDivision() {
        Permanent orca = addCreatureReady(player1, new OrcaSiegeDemon());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, orca.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertInGraveyard(player1, "Orca, Siege Demon");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A creature controlled by Orca's controller dying also adds a counter")
    void ownCreatureDiesPutsCounterOnOrca() {
        Permanent orca = addCreatureReady(player1, new OrcaSiegeDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithMurder(player1, bears.getId());

        assertThat(orca.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void destroyWithMurder(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
