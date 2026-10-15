package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElasIlKorSadisticPilgrim.class, GrizzlyBears.class, Shock.class})
class ElasIlKorSadisticPilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeWhenAllyCreatureEnters() {
        harness.addToBattlefield(player1, new ElasIlKorSadisticPilgrim());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Each opponent loses 1 life when another creature you control dies")
    void allyCreatureDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new ElasIlKorSadisticPilgrim());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature dying")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElasIlKorSadisticPilgrim());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
    @Test
    @DisplayName("Does not gain life for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ElasIlKorSadisticPilgrim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elas il-Kor, Sadistic Pilgrim");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life when an opposing creature enters")
    void opponentCreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElasIlKorSadisticPilgrim());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not drain opponents for its own death")
    void ownDeathDoesNotTrigger() {
        Permanent elas = harness.addToBattlefieldAndReturn(player1, new ElasIlKorSadisticPilgrim());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, elas.getId());

        harness.assertInGraveyard(player1, "Elas il-Kor, Sadistic Pilgrim");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Triggers for every other ally dying simultaneously with Elas")
    void simultaneousDeathsTriggerForEachOtherAlly() {
        Permanent elas = harness.addToBattlefieldAndReturn(player1, new ElasIlKorSadisticPilgrim());
        Permanent firstAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        elas.setMarkedDamage(2);
        firstAlly.setMarkedDamage(2);
        secondAlly.setMarkedDamage(2);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elas il-Kor, Sadistic Pilgrim");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
