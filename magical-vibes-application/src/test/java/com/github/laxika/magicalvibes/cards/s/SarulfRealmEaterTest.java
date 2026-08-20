package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SarulfRealmEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's permanent is put into a graveyard")
    void getsCounterWhenOpponentsPermanentEntersGraveyard() {
        Permanent sarulf = harness.addToBattlefieldAndReturn(player1, new SarulfRealmEater());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(sarulf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when your own permanent is put into a graveyard")
    void doesNotGetCounterWhenOwnPermanentEntersGraveyard() {
        Permanent sarulf = harness.addToBattlefieldAndReturn(player1, new SarulfRealmEater());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(sarulf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing all counters exiles other nonland permanents up to that mana value")
    void removesCountersAndExilesMatchingPermanents() {
        Permanent sarulf = addSarulf(player1, 2);
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sarulf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Sarulf, Realm Eater");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elf.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .doesNotContain(forest.getCard(), airElemental.getCard());
    }

    @Test
    @DisplayName("Declining the upkeep ability keeps Sarulf's counters and permanents")
    void decliningUpkeepAbilityDoesNothing() {
        Permanent sarulf = addSarulf(player1, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sarulf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bears.getCard());
    }

    @Test
    @DisplayName("The upkeep ability does nothing if Sarulf leaves before it resolves")
    void sourceLeavingBeforeResolutionPreventsExile() {
        Permanent sarulf = addSarulf(player1, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sarulf));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bears.getCard());
    }

    private Permanent addSarulf(Player player, int counterCount) {
        Permanent sarulf = harness.addToBattlefieldAndReturn(player, new SarulfRealmEater());
        sarulf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counterCount);
        return sarulf;
    }
}
