package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HissingIguanar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThromokTheInsatiable.class, GrizzlyBears.class, HissingIguanar.class})
class ThromokTheInsatiableTest extends BaseCardTest {

    @Test
    @DisplayName("Devouring two creatures gives four +1/+1 counters")
    void devourTwoAddsFourCounters() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new ThromokTheInsatiable(), "{3}{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent thromok = findPermanent(player1, "Thromok the Insatiable");
        assertThat(thromok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
    }

    @Test
    void devourOneLeavesUnchosenAndOpposingCreaturesAlone() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new ThromokTheInsatiable(), "{3}{R}{G}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(findPermanent(player1, "Thromok the Insatiable")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void devourThreeAddsNineCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new ThromokTheInsatiable(), "{3}{R}{G}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(findPermanent(player1, "Thromok the Insatiable")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(9);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard(), third.getCard());
    }

    @Test
    void decliningDevourPreservesFodderAndThromokDies() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new ThromokTheInsatiable(), "{3}{R}{G}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Thromok the Insatiable");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player1, "Thromok the Insatiable");
    }

    @Test
    void withoutOwnCreaturesThromokDiesWithoutDevourChoice() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new ThromokTheInsatiable(), "{3}{R}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Thromok the Insatiable");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        harness.assertInGraveyard(player1, "Thromok the Insatiable");
    }

    @Test
    void devouredIguanarSeesOtherSimultaneouslyDevouredCreatureDie() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HissingIguanar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HissingIguanar());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new ThromokTheInsatiable(), "{3}{R}{G}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(findPermanent(player1, "Thromok the Insatiable")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Hissing Iguanar")).isZero();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }
}
