package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RailwayBrawler;
import com.github.laxika.magicalvibes.cards.w.WantedGriffin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnotherRound.class, GrizzlyBears.class, ArmoredArmadillo.class, WantedGriffin.class, RailwayBrawler.class})
class AnotherRoundTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and returns the chosen creatures when X is zero")
    void flickersChosenCreaturesOnce() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent notChosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAnotherRound(0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(notChosen.getId())
                .doesNotContain(chosen.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Repeats the process X more times and allows a different choice each time")
    void repeatsForXMoreTimes() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAnotherRound(1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    void canChooseNothingBeforeFlickeringOnALaterIteration() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        castAnotherRound(1);

        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .extracting(Permanent::getId).doesNotContain(creature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canFlickerTheReturnedCreatureOnEveryIteration() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        castAnotherRound(2);

        for (int iteration = 0; iteration < 3; iteration++) {
            var previousId = creature.getId();
            harness.handleMultiplePermanentsChosen(player1, List.of(previousId));
            creature = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(creature.getId()).isNotEqualTo(previousId);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void returnsStolenCreatureToItsOwnerAndLeavesOpposingCreaturesAlone() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        gd.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                java.util.UUID.randomUUID(), "Control effect", null, player1.getId(),
                new com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect(
                        com.github.laxika.magicalvibes.model.effect.ControlDuration.PERMANENT),
                stolen.getId(), null, null, com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT, 0));
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ArmoredArmadillo());
        castAnotherRound(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(stolen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .extracting(Permanent::getId).contains(opposing.getId()).doesNotContain(stolen.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returningCreatureLosesCountersDamageAndTappedState() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setMarkedDamage(1);
        creature.tap();
        castAnotherRound(0);

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void exiledCreatureTokenDoesNotReturn() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        griffin.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        Permanent mercenary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Mercenary"))
                .findFirst().orElseThrow();
        castAnotherRound(0);

        harness.handleMultiplePermanentsChosen(player1, List.of(mercenary.getId()));

        harness.assertNotOnBattlefield(player1, "Mercenary");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void creaturesReturningTogetherSeeEachOthersEntry() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new RailwayBrawler());
        castAnotherRound(0);

        harness.handleMultiplePermanentsChosen(player1, List.of(griffin.getId(), brawler.getId()));
        harness.passBothPriorities();

        Permanent returnedGriffin = findPermanent(player1, "Wanted Griffin");
        assertThat(returnedGriffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void entryTriggersWaitUntilAllIterationsFinish() {
        harness.addToBattlefield(player1, new RailwayBrawler());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new WantedGriffin());
        castAnotherRound(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(griffin.getId()));
        Permanent returnedGriffin = findPermanent(player1, "Wanted Griffin");
        assertThat(returnedGriffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(returnedGriffin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void resolvesWithoutCreaturesEvenWhenXIsPositive() {
        castAnotherRound(2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Another Round");
    }

    private void castAnotherRound(int xValue) {
        harness.setHand(player1, List.of(new AnotherRound()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2 + 2 * xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }
}
