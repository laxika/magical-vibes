package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicQuartermaster.class, PersistentSpecimen.class, Plains.class})
class AngelicQuartermasterTest extends BaseCardTest {

    @Test
    void putsCounterOnEachOfTwoOtherTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        castWithTargets(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canEnterWithoutTargets() {
        castWithTargets(List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angelic Quartermaster");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> castWithTargets(List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetItself() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        castWithTargets(List.of());
        harness.passBothPriorities();

        UUID quartermasterId = harness.getPermanentId(player1, "Angelic Quartermaster");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);

        assertThat(choice.validPermanentIds()).contains(otherCreature.getId()).doesNotContain(quartermasterId);
    }

    @Test
    void canChooseOneOpponentsCreatureWhileLeavingAnotherUntargeted() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        castWithTargets(List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canDeclineTargetsEvenWhenOtherCreaturesAreAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        castWithTargets(List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angelic Quartermaster");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillPutsCounterOnRemainingTargetWhenOneTargetLeaves() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());

        castWithTargets(List.of(removed.getId(), remaining.getId()));
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removed);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Persistent Specimen");
        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterQuartermasterLeavesTheBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        castWithTargets(List.of(creature.getId()));
        harness.passBothPriorities();
        UUID quartermasterId = harness.getPermanentId(player1, "Angelic Quartermaster");
        Permanent quartermaster = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(quartermasterId))
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, quartermaster);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angelic Quartermaster");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersWhenEnteringWithoutBeingCastAndCannotChooseSameCreatureTwice() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());

        Permanent quartermaster = harness.enterBattlefieldAndReturn(player1, new AngelicQuartermaster());
        harness.handlePermanentChosen(player1, first.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(second.getId())
                .doesNotContain(first.getId(), quartermaster.getId());

        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(quartermaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castWithTargets(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new AngelicQuartermaster()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetIds);
    }
}
