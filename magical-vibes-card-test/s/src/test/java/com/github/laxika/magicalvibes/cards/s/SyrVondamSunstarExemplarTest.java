package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Hylderblade;
import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyrVondamSunstarExemplar.class, IntrepidTenderfoot.class, Forest.class, Hylderblade.class})
class SyrVondamSunstarExemplarTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control dying puts a counter on Syr Vondam and gains you life")
    void allyCreatureDyingTriggersCounterAndLife() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());

        removeToGraveyard(bears);

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Another creature you control being exiled triggers, but returning it to hand does not")
    void allyCreatureExiledTriggersButOtherLeavesDoNot() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent exiled = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());

        removeToExile(exiled);

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        Permanent bounced = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bounced));
        harness.passBothPriorities();

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Another player's creature does not trigger Syr Vondam")
    void opponentCreatureDoesNotTrigger() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());

        removeToGraveyard(opponentCreature);

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("At four power, Syr Vondam's death trigger destroys a nonland permanent")
    void highPowerDeathDestroysTarget() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        addTwoCounters(vondam);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());

        removeToGraveyard(vondam);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("At four power, Syr Vondam's death trigger may choose no target")
    void highPowerDeathMayDeclineTarget() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        addTwoCounters(vondam);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());

        removeToGraveyard(vondam);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Syr Vondam's removal trigger does not fire below four power")
    void lowPowerRemovalTriggerDoesNotFire() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());

        removeToGraveyard(vondam);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("At four power, exiling Syr Vondam offers an optional nonland target")
    void highPowerExileDestroysChosenNonlandPermanent() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        addTwoCounters(vondam);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        removeToExile(vondam);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target).contains(land);
    }

    @Test
    @DisplayName("At four power, Syr Vondam's exile trigger may choose no target")
    void highPowerExileMayDeclineTarget() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        addTwoCounters(vondam);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());

        removeToExile(vondam);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Equipment-granted power qualifies Syr Vondam's death trigger")
    void equipmentPowerAtDeathQualifiesEvenAfterEquipmentLeaves() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new Hylderblade());
        blade.setAttachedTo(vondam.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());

        removeToGraveyard(vondam);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, blade));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Equipment-granted power qualifies Syr Vondam's exile trigger")
    void equipmentPowerAtExileQualifiesEvenAfterEquipmentLeaves() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new Hylderblade());
        blade.setAttachedTo(vondam.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());

        removeToExile(vondam);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, blade));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Life gain still resolves if Syr Vondam leaves before its ally-death trigger resolves")
    void gainsLifeWhenSourceLeavesBeforeResolution() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ally));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, vondam));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInHand(player1, "Syr Vondam, Sunstar Exemplar");
    }

    @Test
    @DisplayName("Exiling Syr Vondam below four power does not trigger either ability")
    void lowPowerExileDoesNotTriggerOrGainLife() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());

        removeToExile(vondam);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A noncreature you control dying or being exiled does not trigger Syr Vondam")
    void noncreatureRemovalDoesNotTrigger() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        removeToGraveyard(harness.addToBattlefieldAndReturn(player1, new Forest()));
        removeToExile(harness.addToBattlefieldAndReturn(player1, new Forest()));

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }
    @Test
    @DisplayName("Simultaneous exile still triggers life gain for each other creature")
    void simultaneousExileGainsLifeForEachAlly() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        List<Permanent> creatures = List.of(vondam, first, second);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, creatures, () -> creatures.forEach(creature ->
                        harness.getPermanentRemovalService().removePermanentToExile(gd, creature))));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths gain life but do not raise Syr Vondam's power before it dies")
    void simultaneousDeathsGainLifeWithoutQualifyingRemovalTrigger() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        List<Permanent> creatures = List.of(vondam, first, second);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, creatures, () -> creatures.forEach(creature ->
                        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature))));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }
    private void addTwoCounters(Permanent vondam) {
        removeToGraveyard(harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot()));
        removeToGraveyard(harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot()));
        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private void removeToExile(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        harness.passBothPriorities();
    }
}
