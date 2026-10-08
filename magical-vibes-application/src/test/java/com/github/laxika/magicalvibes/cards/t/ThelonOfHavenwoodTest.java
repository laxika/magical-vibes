package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FungusSliver;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThelonOfHavenwood.class, FungusSliver.class, AshcoatBear.class})
class ThelonOfHavenwoodTest extends BaseCardTest {

    @Test
    void eachFungusGetsBoostFromItsOwnSporeCounters() {
        harness.addToBattlefield(player1, new ThelonOfHavenwood());
        Permanent ownFungus = harness.addToBattlefieldAndReturn(player1, new FungusSliver());
        Permanent opponentFungus = harness.addToBattlefieldAndReturn(player2, new FungusSliver());
        Permanent nonFungus = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());

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
        Permanent nonFungus = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
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

    @Test
    void boostUpdatesWhenSporeCountersAreRemovedAndThelonLeaves() {
        Permanent thelon = harness.addToBattlefieldAndReturn(player1, new ThelonOfHavenwood());
        Permanent fungus = harness.addToBattlefieldAndReturn(player2, new FungusSliver());
        int basePower = gqs.getEffectivePower(gd, fungus);
        int baseToughness = gqs.getEffectiveToughness(gd, fungus);
        fungus.setCounterCount(CounterType.FUNGUS, 3);
        fungus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, fungus)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, fungus)).isEqualTo(baseToughness + 4);
        fungus.setCounterCount(CounterType.FUNGUS, 1);
        assertThat(gqs.getEffectivePower(gd, fungus)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, fungus)).isEqualTo(baseToughness + 2);

        gd.playerBattlefields.get(player1.getId()).remove(thelon);
        harness.setGraveyard(player1, List.of(thelon.getCard()));

        assertThat(gqs.getEffectivePower(gd, fungus)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, fungus)).isEqualTo(baseToughness + 1);
        assertThat(fungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    void paysOwnGraveyardCostBeforeResolutionAndUsesFungiPresentAtResolution() {
        Permanent thelon = harness.addToBattlefieldAndReturn(player1, new ThelonOfHavenwood());
        thelon.tap();
        Permanent fungus = harness.addToBattlefieldAndReturn(player1, new FungusSliver());
        fungus.setCounterCount(CounterType.FUNGUS, 2);
        FungusSliver graveyardFungus = new FungusSliver();
        harness.setGraveyard(player1, List.of(graveyardFungus));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(thelon), 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardFungus.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(graveyardFungus);
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(fungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).remove(thelon);
        harness.setGraveyard(player1, List.of(thelon.getCard()));
        Permanent lateFungus = harness.addToBattlefieldAndReturn(player2, new FungusSliver());
        harness.passBothPriorities();

        assertThat(fungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(3);
        assertThat(lateFungus.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    void cannotActivateWhenNeitherGraveyardContainsAFungus() {
        Permanent thelon = harness.addToBattlefieldAndReturn(player1, new ThelonOfHavenwood());
        AshcoatBear bear = new AshcoatBear();
        harness.setGraveyard(player2, List.of(bear));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(thelon), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bear);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void cannotSubstituteGenericManaForTheBlackRequirement() {
        Permanent thelon = harness.addToBattlefieldAndReturn(player1, new ThelonOfHavenwood());
        FungusSliver graveyardFungus = new FungusSliver();
        harness.setGraveyard(player1, List.of(graveyardFungus));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(thelon), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardFungus);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
