package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.cards.j.JaceThePerfectedMind;
import com.github.laxika.magicalvibes.cards.v.ViralDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TekuthalInquiryDominus.class, GrizzlyBears.class, IronStar.class, ViralDrake.class,
        Ichthyomorphosis.class, JaceThePerfectedMind.class})
class TekuthalInquiryDominusTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles a proliferate event into two sequential choices")
    void doublesProliferate() {
        harness.addToBattlefield(player1, new TekuthalInquiryDominus());
        harness.addToBattlefield(player1, new ViralDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes three counters from eligible other permanents and adds an indestructible counter")
    void removesThreeCountersFromOtherEligiblePermanents() {
        Permanent tekuthal = harness.addToBattlefieldAndReturn(player1, new TekuthalInquiryDominus());
        tekuthal.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherCreature.setCounterCount(CounterType.OIL, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IronStar());
        artifact.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.OIL)).isZero();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(tekuthal.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, tekuthal, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void canChooseDifferentPermanentsForEachProliferation() {
        Permanent tekuthal = harness.addToBattlefieldAndReturn(player1, new TekuthalInquiryDominus());
        tekuthal.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ViralDrake());
        drake.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(tekuthal.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(drake.getId()));

        assertThat(tekuthal.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(2);
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canDeclineFirstProliferationAndChoosePlayerForSecond() {
        harness.addToBattlefield(player1, new TekuthalInquiryDominus());
        harness.addToBattlefield(player1, new ViralDrake());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void energyOnlyPlayerRemainsEligibleForSecondProliferation() {
        harness.addToBattlefield(player1, new TekuthalInquiryDominus());
        harness.addToBattlefield(player1, new ViralDrake());
        gd.setPlayerEnergyCounters(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void losingAbilitiesStopsProliferationReplacement() {
        Permanent tekuthal = harness.addToBattlefieldAndReturn(player1, new TekuthalInquiryDominus());
        harness.addToBattlefield(player1, new ViralDrake());
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, tekuthal.getId());
        harness.passBothPriorities();
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysWithBlueManaAndRemovesThreeCountersFromOnePermanent() {
        Permanent tekuthal = harness.addToBattlefieldAndReturn(player1, new TekuthalInquiryDominus());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new IronStar());
        other.setCounterCount(CounterType.INDESTRUCTIBLE, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(other.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(tekuthal.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        assertThat(tekuthal.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void cannotPayWithOwnCountersOrOpponentsCounters() {
        Permanent tekuthal = harness.addToBattlefieldAndReturn(player1, new TekuthalInquiryDominus());
        tekuthal.setCounterCount(CounterType.INDESTRUCTIBLE, 3);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new TekuthalInquiryDominus());
        opponent.setCounterCount(CounterType.INDESTRUCTIBLE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tekuthal.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(3);
        assertThat(opponent.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDoubleOpponentsProliferation() {
        harness.addToBattlefield(player2, new TekuthalInquiryDominus());
        harness.addToBattlefield(player1, new ViralDrake());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void requiresChoiceOfCounterKindsWhenPayingFromOnePermanent() {
        harness.addToBattlefield(player1, new TekuthalInquiryDominus());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IronStar());
        artifact.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        artifact.setCounterCount(CounterType.OIL, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    void canRemoveLoyaltyCountersFromAnotherPlaneswalker() {
        Permanent tekuthal = harness.addToBattlefieldAndReturn(player1, new TekuthalInquiryDominus());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceThePerfectedMind());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 16);
        harness.passBothPriorities();
        assertThat(tekuthal.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }
}
