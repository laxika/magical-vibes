package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObNixilisReignited;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PramikonSkyRampart.class, GrizzlyBears.class, Card.class, ObNixilisReignited.class})
class PramikonSkyRampartTest extends BaseCardTest {

    @Test
    @DisplayName("ETB direction limits attacks to the nearest opponent and their planeswalkers")
    void etbDirectionLimitsAttackTargets() {
        UUID player3Id = addThirdPlayer();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card planeswalkerCard = new Card();
        planeswalkerCard.setName("Test Planeswalker");
        planeswalkerCard.setType(CardType.PLANESWALKER);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.enterBattlefieldAndReturn(player1, new PramikonSkyRampart());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        harness.passBothPriorities();

        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, planeswalker.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    private UUID addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        return player3Id;
    }

    @Test
    @DisplayName("Choosing a direction does not put an ability on the stack")
    void directionChoiceDoesNotUseTheStack() {
        UUID player3Id = addThirdPlayer();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new PramikonSkyRampart());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");

        assertThat(gd.stack).isEmpty();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    @Test
    @DisplayName("Left prevents attacks on the other opponent and their planeswalkers")
    void leftRestrictsPlayersAndPlaneswalkers() {
        UUID player3Id = addThirdPlayer();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ObNixilisReignited());
        harness.enterBattlefieldAndReturn(player1, new PramikonSkyRampart());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Left");
        resolveAllTriggers();

        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, attacker, planeswalker.getId())).isFalse();
    }

    @Test
    @DisplayName("Opposite directions from different Pramikons prevent attacks with three players")
    void opposingDirectionsPreventAttacks() {
        UUID player3Id = addThirdPlayer();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new PramikonSkyRampart());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player2, new PramikonSkyRampart());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Left");
        resolveAllTriggers();

        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    @Test
    @DisplayName("The attack restriction ends when Pramikon leaves the battlefield")
    void restrictionEndsWhenPramikonLeaves() {
        UUID player3Id = addThirdPlayer();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent pramikon = harness.enterBattlefieldAndReturn(player1, new PramikonSkyRampart());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        resolveAllTriggers();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(pramikon);

        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isTrue();
    }

    @Test
    @DisplayName("Right also restricts an opponent's creatures relative to their own seat")
    void directionAppliesToEachPlayersOwnSeat() {
        UUID player3Id = addThirdPlayer();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new PramikonSkyRampart());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        resolveAllTriggers();

        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Opposite directions still allow attacks when only two players are present")
    void oppositeDirectionsAllowAttacksInTwoPlayerGame() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new PramikonSkyRampart());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player2, new PramikonSkyRampart());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Left");
        resolveAllTriggers();

        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
    }
}
