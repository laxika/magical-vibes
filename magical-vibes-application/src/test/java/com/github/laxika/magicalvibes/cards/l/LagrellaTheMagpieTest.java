package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LagrellaTheMagpie.class, GrizzlyBears.class, Unsummon.class})
class LagrellaTheMagpieTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any number of other target creatures, at most one per controller")
    void exilesTargetCreaturesControlledByDifferentPlayers() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLagrella(List.of(ownBear.getId(), opposingBear.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target two creatures controlled by the same player")
    void cannotTargetTwoCreaturesControlledBySamePlayer() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareLagrellaCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Returned creatures controlled by you get two +1/+1 counters")
    void returnedOwnCreatureGetsCountersButOpponentsCreatureDoesNot() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLagrella(List.of(ownBear.getId(), opposingBear.getId()));

        UUID lagrellaId = harness.getPermanentId(player1, "Lagrella, the Magpie");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, lagrellaId);
        harness.passBothPriorities();

        Permanent returnedOwnBear = findPermanent(player1, "Grizzly Bears");
        Permanent returnedOpposingBear = findPermanent(player2, "Grizzly Bears");
        assertThat(returnedOwnBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returnedOpposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Choosing no targets leaves other creatures on the battlefield")
    void canChooseNoTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castLagrella(List.of());

        harness.assertOnBattlefield(player1, "Lagrella, the Magpie");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targets are not exiled if Lagrella leaves before its enter trigger resolves")
    void sourceLeavingBeforeTriggerResolvesPreventsExile() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareLagrellaCast();
        harness.castCreature(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        UUID lagrellaId = harness.getPermanentId(player1, "Lagrella, the Magpie");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, lagrellaId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lagrella, the Magpie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counters are added by a separate trigger after the creature returns")
    void returnedCreatureCanBeRemovedBeforeCounterTriggerResolves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castLagrella(List.of(bear.getId()));
        UUID lagrellaId = harness.getPermanentId(player1, "Lagrella, the Magpie");
        harness.setHand(player1, List.of(new Unsummon(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, lagrellaId);

        UUID returnedId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, returnedId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    private void castLagrella(List<UUID> targetIds) {
        prepareLagrellaCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareLagrellaCast() {
        harness.setHand(player1, List.of(new LagrellaTheMagpie()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
