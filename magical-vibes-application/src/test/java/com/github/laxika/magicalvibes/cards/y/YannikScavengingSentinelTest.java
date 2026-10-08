package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NikaraLairScavenger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YannikScavengingSentinel.class, NikaraLairScavenger.class, GrizzlyBears.class, YotianSoldier.class})
class YannikScavengingSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Nikara")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card nikara = new NikaraLairScavenger();
        harness.setLibrary(player2, List.of(nikara));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new YannikScavengingSentinel());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(nikara);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles another creature and distributes counters equal to its power")
    void exilesCreatureAndDistributesItsPower() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of());
        Permanent exiledCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new YotianSoldier());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new YotianSoldier());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        gd.pendingETBDamageAssignments = Map.of(firstTarget.getId(), 1, secondTarget.getId(), 1);

        harness.enterBattlefieldAndReturn(player1, new YannikScavengingSentinel());
        resolvePartnerWithNoSearch();

        harness.passBothPriorities();
        PendingInteraction.PermanentChoice exileChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(exileChoice.validPermanentIds()).contains(exiledCreature.getId());
        harness.handlePermanentChosen(player1, exiledCreature.getId());

        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCreature.getCard());
    }

    private void resolvePartnerWithNoSearch() {
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (targetChoice != null && targetChoice.validPlayerIds().contains(player2.getId())) {
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, false);
        }
    }

    @Test
    void doesNotExileWhenYannikLeavesBeforeEntryTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent yannik = harness.enterBattlefieldAndReturn(player1, new YannikScavengingSentinel());
        resolvePartnerWithNoSearch();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yannik));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void zeroTargetsStillExilesAndReturnsCreatureWhenYannikLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent yannik = harness.enterBattlefieldAndReturn(player1, new YannikScavengingSentinel());
        resolvePartnerWithNoSearch();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
        assertThat(yannik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yannik));

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(returned -> {
                    assertThat(returned.getCard()).isSameAs(creature.getCard());
                    assertThat(returned.getId()).isNotEqualTo(creature.getId());
                });
    }

    @Test
    void usesPowerIncludingCountersAndCanPutCountersOnYannik() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent yannik = harness.enterBattlefieldAndReturn(player1, new YannikScavengingSentinel());
        gd.pendingETBDamageAssignments = Map.of(yannik.getId(), 5);
        resolvePartnerWithNoSearch();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, yannik.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(yannik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
    }

    @Test
    void countersAssignedToRemovedTargetAreNotRedistributed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player2, new YotianSoldier());
        Permanent yannik = harness.enterBattlefieldAndReturn(player1, new YannikScavengingSentinel());
        gd.pendingETBDamageAssignments = Map.of(yannik.getId(), 1, otherTarget.getId(), 1);
        resolvePartnerWithNoSearch();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, yannik.getId());
        harness.handlePermanentChosen(player1, otherTarget.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, otherTarget));
        harness.passBothPriorities();

        assertThat(yannik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
    }
}
