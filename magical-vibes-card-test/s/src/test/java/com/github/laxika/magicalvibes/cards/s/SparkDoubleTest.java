package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.f.FiremindVessel;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LazotepReaver;
import com.github.laxika.magicalvibes.cards.t.TomikDistinguishedAdvokist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparkDouble.class, ChoMannoRevolutionary.class, JaceBeleren.class, GrizzlyBears.class,
        GideonBlackblade.class, TomikDistinguishedAdvokist.class, FiremindVessel.class, LazotepReaver.class})
class SparkDoubleTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a controlled creature with an additional +1/+1 counter")
    void copiesControlledCreatureWithCounterAndNoLegendarySupertype() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ChoMannoRevolutionary());

        Permanent copy = castAndChoose(target);

        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Copies a controlled planeswalker with its printed loyalty plus one")
    void copiesControlledPlaneswalkerWithAdditionalLoyalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);

        Permanent copy = castAndChoose(target);

        assertThat(copy.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot copy a permanent controlled by an opponent")
    void cannotCopyOpponentPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        SparkDouble sparkDouble = new SparkDouble();
        castSparkDouble(sparkDouble);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(sparkDouble.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sparkDouble.getId()));
    }

    @Test
    @DisplayName("Declining to copy gives no counter, so Spark Double dies")
    void decliningCopyDoesNotReceiveCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TomikDistinguishedAdvokist());
        SparkDouble sparkDouble = new SparkDouble();
        castSparkDouble(sparkDouble);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "Spark Double");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A nonlegendary copy coexists with the legendary original without copying its counters or tapped state")
    void legendaryCreatureCopyDoesNotCopyCountersOrTappedState() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TomikDistinguishedAdvokist());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        target.tap();

        Permanent copy = castAndChoose(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(target, copy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
    }

    @Test
    @DisplayName("Copying another Spark Double copy preserves the copied creature but adds only one counter")
    void copyingCopyAddsOnlyOneCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TomikDistinguishedAdvokist());
        Permanent firstCopy = castAndChoose(target);

        Permanent secondCopy = castAndChoose(firstCopy);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactlyInAnyOrder(target, firstCopy, secondCopy);
        assertThat(secondCopy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, secondCopy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondCopy)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Copying Gideon Blackblade during your turn adds both kinds of counter")
    void copyingGideonDuringYourTurnAddsBothCounters() {
        Permanent target = harness.enterBattlefieldAndReturn(player1, new GideonBlackblade());

        Permanent copy = castAndChoose(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(target, copy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(copy.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Planeswalker copies use starting loyalty rather than the original's current counters")
    void doesNotCopyCurrentLoyaltyCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 8);

        Permanent copy = castAndChoose(target);

        assertThat(copy.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(target, copy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An artifact that is neither a creature nor a planeswalker is not a copy choice")
    void cannotCopyNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FiremindVessel());

        castSparkDouble(new SparkDouble());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(artifact);
        harness.assertInGraveyard(player1, "Spark Double");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The copied creature's enters ability triggers")
    void copiedEntersAbilityTriggers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LazotepReaver());

        Permanent copy = castAndChoose(target);
        resolveAllTriggers();

        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent != target && permanent != copy)
                .singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castAndChoose(Permanent target) {
        SparkDouble sparkDouble = new SparkDouble();
        castSparkDouble(sparkDouble);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(sparkDouble.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void castSparkDouble(SparkDouble sparkDouble) {
        harness.castFromHand(player1, sparkDouble, "{3}{U}");
        resolveAllTriggers();
    }
}
