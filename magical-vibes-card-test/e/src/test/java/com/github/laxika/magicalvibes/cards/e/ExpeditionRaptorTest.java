package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpeditionRaptor.class, GrizzlyBears.class, Plains.class})
class ExpeditionRaptorTest extends BaseCardTest {

    @Test
    void putsCounterOnOneOtherTargetCreature() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castRaptor(List.of(bear.getId()));

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnEachOfTwoOtherTargetCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        castRaptor(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canEnterWithoutTargets() {
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Expedition Raptor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseZeroTargetsEvenWhenOtherCreaturesExist() {
        Permanent otherRaptor = addCreatureReady(player1, new ExpeditionRaptor());

        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(otherRaptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Expedition Raptor")).hasSize(2)
                .allSatisfy(raptor -> assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canSupportAnotherExpeditionRaptorButDoesNotSupportItself() {
        Permanent otherRaptor = addCreatureReady(player1, new ExpeditionRaptor());

        castRaptor(List.of(otherRaptor.getId()));

        assertThat(otherRaptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Expedition Raptor")).hasSize(2)
                .filteredOn(raptor -> !raptor.getId().equals(otherRaptor.getId()))
                .allSatisfy(raptor -> assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void cannotChooseItselfWhenSelectingTargetsAfterEntering() {
        Permanent otherRaptor = addCreatureReady(player1, new ExpeditionRaptor());
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent enteringRaptor = findPermanents(player1, "Expedition Raptor").stream()
                .filter(raptor -> !raptor.getId().equals(otherRaptor.getId()))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enteringRaptor.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        assertThat(enteringRaptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent otherRaptor = addCreatureReady(player1, new ExpeditionRaptor());
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(otherRaptor.getId(), otherRaptor.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseMoreThanTwoCreatures() {
        Permanent first = addCreatureReady(player1, new ExpeditionRaptor());
        Permanent second = addCreatureReady(player1, new ExpeditionRaptor());
        Permanent third = addCreatureReady(player2, new ExpeditionRaptor());
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillSupportsRemainingTargetWhenOneTargetLeavesBeforeResolution() {
        Permanent first = addCreatureReady(player1, new ExpeditionRaptor());
        Permanent second = addCreatureReady(player2, new ExpeditionRaptor());
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();
        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(first);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castRaptor(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();

        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
