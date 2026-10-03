package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClashOfTitans.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, GiantGrowth.class})
class ClashOfTitansTest extends BaseCardTest {

    @Test
    void targetCreaturesFightEachOther() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ClashOfTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, List.of(bearsId, giantId));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canTargetTwoCreaturesControlledByTheSamePlayer() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ClashOfTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(giantId, elvesId));

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void cannotTargetTheSameCreatureTwice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClashOfTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    void bothCreaturesDealLethalDamageEvenWhenTapped() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setTapped(true);
        second.setTapped(true);
        harness.setHand(player1, List.of(new ClashOfTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Clash of Titans");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void neitherCreatureDealsDamageWhenEitherTargetLeavesBeforeResolution(boolean removeFirst) {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ClashOfTitans()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        Permanent removed = removeFirst ? first : second;
        Permanent survivor = removeFirst ? second : first;
        harness.getPermanentRemovalService().removePermanentToHand(gd, removed);
        harness.passBothPriorities();

        assertThat(survivor.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(removeFirst ? player2 : player1, survivor.getCard().getName());
        harness.assertInHand(removeFirst ? player1 : player2, removed.getCard().getName());
        harness.assertInGraveyard(player1, "Clash of Titans");
    }

    @Test
    void fightUsesPowerAfterAResponseResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ClashOfTitans(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(bears.getId(), giant.getId()));

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Clash of Titans");
        harness.assertInGraveyard(player1, "Giant Growth");
    }
}
