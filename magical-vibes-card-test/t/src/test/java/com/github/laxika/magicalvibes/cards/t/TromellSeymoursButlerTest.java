package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TromellSeymoursButler.class, DragonFodder.class, GrizzlyBears.class})
class TromellSeymoursButlerTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken creatures you control enter with an additional +1/+1 counter")
    void nontokenCreatureEntersWithAdditionalCounter() {
        harness.addToBattlefield(player1, new TromellSeymoursButler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability proliferates once per nontoken creature entered this turn")
    void proliferatesForNontokenCreaturesEnteredThisTurn() {
        Permanent tromell = addCreatureReady(player1, new TromellSeymoursButler());

        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(tromell.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void tromellDoesNotGiveItselfAnEntryCounter() {
        Permanent tromell = harness.enterBattlefieldAndReturn(player1, new TromellSeymoursButler());

        assertThat(tromell.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsCreaturesDoNotReceiveEntryCounters() {
        harness.addToBattlefield(player1, new TromellSeymoursButler());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new TromellSeymoursButler());

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void creatureTokensDoNotReceiveEntryCounters() {
        harness.addToBattlefield(player1, new TromellSeymoursButler());
        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(2)
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void creaturesThatLeftBeforeResolutionDoNotIncreaseX() {
        Permanent tromell = addCreatureReady(player1, new TromellSeymoursButler());
        tromell.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(tromell.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void multipleProliferationsAllowDifferentChoices() {
        addCreatureReady(player1, new TromellSeymoursButler());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void laterProliferationsCanChoosePlayersWithOnlyEnergyAndExperienceCounters() {
        addCreatureReady(player1, new TromellSeymoursButler());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.setPlayerEnergyCounters(player1.getId(), 1);
        gd.playerExperienceCounters.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void tromellCountsItsOwnEntryAndCanProliferateOpponentCounters() {
        Permanent tromell = harness.enterBattlefieldAndReturn(player1, new TromellSeymoursButler());
        tromell.setSummoningSick(false);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TromellSeymoursButler());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId(), player2.getId()));

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayChooseNothingForEachProliferation() {
        addCreatureReady(player1, new TromellSeymoursButler());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
