package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimefeatherOwl.class, SnowCoveredPlains.class, MishrasBauble.class})
class RimefeatherOwlTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of snow permanents on the battlefield")
    void ptEqualsSnowPermanentCount() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        addSnowPermanent(player1);
        addSnowPermanent(player2);
        addNonSnowPermanent(player1);

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(3);
    }

    @Test
    @DisplayName("An ice counter makes the target permanent snow")
    void iceCounterMakesTargetSnow() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = addNonSnowPermanent(player1);
        prepareSnowActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(2);
    }

    @Test
    @DisplayName("An ice counter can make an opponent's permanent snow")
    void iceCounterMakesOpponentsPermanentSnow() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = addNonSnowPermanent(player2);
        prepareSnowActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability requires snow mana")
    void requiresSnowMana() {
        harness.addToBattlefield(player1, new RimefeatherOwl());
        Permanent target = addNonSnowPermanent(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations count a permanent once, not once per ice counter")
    void repeatedIceCountersDoNotIncreaseSnowCount() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        prepareSnowActivation();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        prepareSnowActivation();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adding an ice counter to an already snow permanent does not increase the count")
    void iceCounterOnSnowPermanentDoesNotIncreaseSnowCount() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = addSnowPermanent(player2);
        prepareSnowActivation();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(2);
    }

    @Test
    @DisplayName("Existing ice counters grant snow only while an Owl is on the battlefield")
    void existingIceCountersDependOnOwlBeingPresent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        target.setCounterCount(CounterType.ICE, 2);
        Permanent naturalSnow = addSnowPermanent(player2);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();

        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, owl));
        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
        assertThat(gqs.hasEffectiveSupertype(gd, naturalSnow, CardSupertype.SNOW)).isTrue();

        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing the last ice counter removes snow and immediately reduces the Owl's size")
    void removingLastIceCounterUpdatesSnowCount() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        target.setCounterCount(CounterType.ICE, 2);
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);

        target.setCounterCount(CounterType.ICE, 1);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isTrue();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(2);

        target.setCounterCount(CounterType.ICE, 0);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability still places its counter after the Owl leaves")
    void activationResolvesAfterOwlLeaves() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RimefeatherOwl());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        prepareSnowActivation();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, owl));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
    }

    @Test
    @DisplayName("The size-defining ability works in hand and graveyard without granting snow from those zones")
    void sizeAbilityWorksOutsideBattlefield() {
        RimefeatherOwl owl = new RimefeatherOwl();
        harness.setHand(player1, List.of(owl));
        addSnowPermanent(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        target.setCounterCount(CounterType.ICE, 1);

        assertThat(gqs.getEffectiveCardPower(gd, owl)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, owl)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(owl));
        addSnowPermanent(player2);
        assertThat(gqs.getEffectiveCardPower(gd, owl)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, owl)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.SNOW)).isFalse();
    }

    private void prepareSnowActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

    private Permanent addSnowPermanent(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredPlains());
    }

    private Permanent addNonSnowPermanent(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SnowCoveredPlains());
        TestCards.mutableCard(permanent).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        return permanent;
    }
}
