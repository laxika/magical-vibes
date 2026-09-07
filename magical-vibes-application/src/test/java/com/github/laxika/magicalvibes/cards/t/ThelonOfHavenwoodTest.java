package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FungusSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThelonOfHavenwood.class, FungusSliver.class, GrizzlyBears.class})
class ThelonOfHavenwoodTest extends BaseCardTest {

    @Test
    void eachFungusGetsBoostFromItsOwnSporeCounters() {
        harness.addToBattlefield(player1, new ThelonOfHavenwood());
        Permanent ownFungus = harness.addToBattlefieldAndReturn(player1, new FungusSliver());
        Permanent opponentFungus = harness.addToBattlefieldAndReturn(player2, new FungusSliver());
        Permanent nonFungus = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        int ownBasePower = gqs.getEffectivePower(gd, ownFungus);
        int ownBaseToughness = gqs.getEffectiveToughness(gd, ownFungus);
        int opponentBasePower = gqs.getEffectivePower(gd, opponentFungus);
        int nonFungusBasePower = gqs.getEffectivePower(gd, nonFungus);
        ownFungus.setCounterCount(CounterType.FUNGUS, 2);
        opponentFungus.setCounterCount(CounterType.FUNGUS, 1);
        nonFungus.setCounterCount(CounterType.FUNGUS, 3);

        assertThat(gqs.getEffectivePower(gd, ownFungus)).isEqualTo(ownBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, ownFungus)).isEqualTo(ownBaseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, opponentFungus)).isEqualTo(opponentBasePower + 1);
        assertThat(gqs.getEffectivePower(gd, nonFungus)).isEqualTo(nonFungusBasePower);
    }

    @Test
    void exilesFungusFromAnyGraveyardAndPutsCountersOnAllFungi() {
        Permanent thelon = harness.addToBattlefieldAndReturn(player1, new ThelonOfHavenwood());
        Permanent ownFungus = harness.addToBattlefieldAndReturn(player1, new FungusSliver());
        Permanent opponentFungus = harness.addToBattlefieldAndReturn(player2, new FungusSliver());
        Permanent nonFungus = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FungusSliver graveyardFungus = new FungusSliver();
        harness.setGraveyard(player2, List.of(graveyardFungus));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(thelon), 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardFungus.getId()));
        harness.passBothPriorities();

        assertThat(ownFungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(opponentFungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(nonFungus.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
