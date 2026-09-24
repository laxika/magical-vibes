package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulSliver.class, BonescytheSliver.class, GrizzlyBears.class, Shock.class, JaceBeleren.class})
class SpitefulSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Spiteful Sliver reflects damage dealt to itself at a chosen player")
    void reflectsDamageDealtToItself() {
        Permanent spitefulSliver = addCreatureReady(player1, new SpitefulSliver());
        harness.setLife(player2, 20);
        shock(spitefulSliver, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Spiteful Sliver");
    }

    @Test
    @DisplayName("Spiteful Sliver grants the damage trigger to another Sliver you control")
    void grantsDamageTriggerToAnotherSliver() {
        addCreatureReady(player1, new SpitefulSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        harness.setLife(player2, 20);
        shock(otherSliver, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Spiteful Sliver does not grant the trigger to non-Slivers")
    void doesNotGrantDamageTriggerToNonSlivers() {
        addCreatureReady(player1, new SpitefulSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spiteful Sliver can target a planeswalker")
    void reflectsDamageToPlaneswalker() {
        Permanent spitefulSliver = addCreatureReady(player1, new SpitefulSliver());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        shock(spitefulSliver, jace.getId());

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    private void shock(Permanent creature, java.util.UUID reflectedTargetId) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, reflectedTargetId);
        harness.passBothPriorities();
    }
}
