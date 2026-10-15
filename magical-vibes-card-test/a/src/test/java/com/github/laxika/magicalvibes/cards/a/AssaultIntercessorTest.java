package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.Exterminatus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AssaultIntercessor.class, Exterminatus.class, GrizzlyBears.class, Shock.class})
class AssaultIntercessorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent loses two life when their creature dies")
    void opponentLosesTwoLifeWhenTheirCreatureDies() {
        harness.addToBattlefield(player1, new AssaultIntercessor());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not trigger when your own creature dies")
    void doesNotTriggerWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new AssaultIntercessor());
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Intercessor sees every opposing creature dying simultaneously with it")
    void triggersForOpposingCreaturesDyingSimultaneouslyWithSource() {
        harness.addToBattlefield(player1, new AssaultIntercessor());
        harness.addToBattlefield(player2, new AssaultIntercessor());
        harness.addToBattlefield(player2, new AssaultIntercessor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Exterminatus()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Assault Intercessor");
        harness.assertNotOnBattlefield(player2, "Assault Intercessor");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Each copy triggers independently for an opposing creature death")
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new AssaultIntercessor());
        harness.addToBattlefield(player1, new AssaultIntercessor());
        var creature = harness.addToBattlefieldAndReturn(player2, new AssaultIntercessor());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A queued Chainsword trigger resolves after its source dies")
    void queuedTriggerResolvesAfterSourceDies() {
        var source = harness.addToBattlefieldAndReturn(player1, new AssaultIntercessor());
        var creature = harness.addToBattlefieldAndReturn(player2, new AssaultIntercessor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Assault Intercessor");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
