package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArcBlade;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiftElemental.class, ArcBlade.class, BlindPhantasm.class, RavagingRiftwurm.class})
class RiftElementalTest extends BaseCardTest {

    @Test
    void removesTimeCounterFromControlledPermanentAndBoostsSelf() {
        Permanent source = readySource();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        target.setCounterCount(CounterType.TIME, 1);
        addActivationMana();

        activate(source);

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isZero();
    }

    @Test
    void removesTimeCounterFromOwnedSuspendedCard() {
        Permanent source = readySource();
        ArcBlade target = suspendedCard(player1, 2);
        addActivationMana();

        activate(source);

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
        assertThat(source.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void choosesBetweenControlledPermanentAndSuspendedCard() {
        Permanent source = readySource();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        permanent.setCounterCount(CounterType.TIME, 1);
        ArcBlade suspended = suspendedCard(player1, 2);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(source), null, null);

        PendingInteraction.RemoveTimeCounterCostChoice choice =
                (PendingInteraction.RemoveTimeCounterCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(permanent.getCard().getId(), suspended.getId());
        harness.handleMultipleCardsChosen(player1, List.of(suspended.getId()));
        harness.passBothPriorities();

        assertThat(permanent.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(suspended.getId(), 1);
        assertThat(source.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void removingLastSuspendedTimeCounterOffersItsCast() {
        Permanent source = readySource();
        ArcBlade target = suspendedCard(player1, 1);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(source), null, null);

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(source.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
    }

    @Test
    void cannotActivateWithoutAnEligibleTimeCounter() {
        Permanent source = readySource();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(source), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("time counter");
    }

    @Test
    void cannotActivateUsingOpponentControlledPermanent() {
        Permanent source = readySource();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        target.setCounterCount(CounterType.TIME, 1);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(source), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("time counter");
        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void cannotActivateUsingOpponentOwnedSuspendedCard() {
        Permanent source = readySource();
        ArcBlade target = suspendedCard(player2, 1);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(source), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("time counter");
        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
    }

    @Test
    void cannotActivateUsingNonSuspendTimeCounterOnExiledCard() {
        Permanent source = readySource();
        BlindPhantasm target = new BlindPhantasm();
        harness.setExile(player1, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), 1);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(source), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("time counter");
        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
    }

    @Test
    void nativeSuspendCardWithCountersFromAnotherEffectCanPayCost() {
        Permanent source = readySource();
        ArcBlade target = suspendedCard(player1, 2);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());
        addActivationMana();

        activate(source);

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
        assertThat(source.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void removingLastCounterFromVanishingPermanentTriggersSacrifice() {
        Permanent source = readySource();
        Permanent target = harness.enterBattlefieldAndReturn(player1, new RavagingRiftwurm());
        addActivationMana();
        activate(source);
        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(source), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ravaging Riftwurm");
        harness.assertNotOnBattlefield(player1, "Ravaging Riftwurm");
        harness.passBothPriorities();
        assertThat(source.getPowerModifier()).isEqualTo(4);
    }

    @Test
    void summoningSickTappedSourceCanActivateAndBoostsExpireAtEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RiftElemental());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        target.setCounterCount(CounterType.TIME, 2);
        addActivationMana();
        activate(source);
        addActivationMana();
        activate(source);

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(source.getPowerModifier()).isEqualTo(4);
        assertThat(source.getToughnessModifier()).isZero();
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(source.getPowerModifier()).isZero();
    }

    private Permanent readySource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RiftElemental());
        source.setSummoningSick(false);
        return source;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void activate(Permanent source) {
        harness.activateAbility(player1, battlefieldIndex(source), null, null);
        harness.passBothPriorities();
    }

    private ArcBlade suspendedCard(Player owner, int timeCounters) {
        ArcBlade target = new ArcBlade();
        harness.setExile(owner, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), timeCounters);
        return target;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
