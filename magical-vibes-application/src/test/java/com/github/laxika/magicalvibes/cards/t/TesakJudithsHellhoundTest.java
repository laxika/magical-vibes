package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Dog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TesakJudithsHellhound.class, Dog.class, GrizzlyBears.class})
class TesakJudithsHellhoundTest extends BaseCardTest {

    @Test
    @DisplayName("Tesak's unleash can put a +1/+1 counter on it")
    void tesakCanUnleash() {
        Permanent tesak = enterTesak(true);

        assertThat(tesak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Other Dogs you control can unleash and then can't block")
    void otherDogsGainUnleash() {
        addReadyTesak();
        Permanent dog = harness.enterBattlefieldAndReturn(player1, new Dog());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        addReadyCreature(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures you control with counters have haste")
    void counteredCreatureHasHaste() {
        harness.setLife(player2, 20);
        addReadyTesak();
        Permanent dog = new Permanent(new Dog());
        dog.setSummoningSick(true);
        dog.setCounterCount(CounterType.CHARGE, 1);
        gd.playerBattlefields.get(player1.getId()).add(dog);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Attacking with Tesak adds red mana for each attacking creature")
    void attackAddsManaForEachAttackingCreature() {
        addReadyTesak();
        addReadyCreature(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            resolveAllTriggers();
        });

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
    }

    private Permanent enterTesak(boolean unleash) {
        harness.setHand(player1, List.of(new TesakJudithsHellhound()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
        return findPermanent(player1, "Tesak, Judith's Hellhound");
    }

    private void addReadyTesak() {
        Permanent tesak = harness.addToBattlefieldAndReturn(player1, new TesakJudithsHellhound());
        tesak.setSummoningSick(false);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
