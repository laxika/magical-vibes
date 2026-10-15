package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArixmethesSlumberingIsle.class, MyrRetriever.class, GalvanicBlast.class})
class ArixmethesSlumberingIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with five slumber counters and is a land instead of a creature")
    void entersTappedWithSlumberCounters() {
        Permanent arixmethes = harness.enterBattlefieldAndReturn(player1, new ArixmethesSlumberingIsle());

        assertThat(arixmethes.isTapped()).isTrue();
        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(5);
        assertThat(gqs.isLand(gd, arixmethes)).isTrue();
        assertThat(gqs.isCreature(gd, arixmethes)).isFalse();
    }

    @Test
    @DisplayName("May remove a slumber counter when its controller casts a spell")
    void mayRemoveSlumberCounter() {
        Permanent arixmethes = addCreatureReady(player1, new ArixmethesSlumberingIsle());
        arixmethes.setCounterCount(CounterType.SLUMBER, 1);
        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isZero();
        assertThat(gqs.isLand(gd, arixmethes)).isFalse();
        assertThat(gqs.isCreature(gd, arixmethes)).isTrue();
    }

    @Test
    @DisplayName("May decline removing a slumber counter")
    void mayDeclineRemovingSlumberCounter() {
        Permanent arixmethes = addCreatureReady(player1, new ArixmethesSlumberingIsle());
        arixmethes.setCounterCount(CounterType.SLUMBER, 1);
        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(1);
        assertThat(gqs.isLand(gd, arixmethes)).isTrue();
        assertThat(gqs.isCreature(gd, arixmethes)).isFalse();
    }

    @Test
    @DisplayName("Tapping it adds one green and one blue mana")
    void tappingAddsGreenAndBlueMana() {
        Permanent arixmethes = addCreatureReady(player1, new ArixmethesSlumberingIsle());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(arixmethes.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent casting a spell does not remove slumber counters")
    void opponentSpellDoesNotTrigger() {
        Permanent arixmethes = harness.enterBattlefieldAndReturn(player1, new ArixmethesSlumberingIsle());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new MyrRetriever(), "{2}");
        harness.passBothPriorities();

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Myr Retriever");
    }

    @Test
    @DisplayName("Removing one counter leaves Arixmethes a land while other counters remain")
    void remainsLandWithRemainingCounters() {
        Permanent arixmethes = harness.enterBattlefieldAndReturn(player1, new ArixmethesSlumberingIsle());
        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(4);
        assertThat(gqs.isLand(gd, arixmethes)).isTrue();
        assertThat(gqs.isCreature(gd, arixmethes)).isFalse();
    }

    @Test
    @DisplayName("The cast trigger still works without counters and cannot remove below zero")
    void acceptingWithoutCountersIsHarmless() {
        Permanent arixmethes = addCreatureReady(player1, new ArixmethesSlumberingIsle());
        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isZero();
        assertThat(gqs.isCreature(gd, arixmethes)).isTrue();
        harness.assertOnBattlefield(player1, "Myr Retriever");
    }

    @Test
    @DisplayName("Arixmethes can produce mana the turn it enters while it is a land")
    void sleepingLandIgnoresSummoningSickness() {
        Permanent arixmethes = harness.enterBattlefieldAndReturn(player1, new ArixmethesSlumberingIsle());
        arixmethes.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(arixmethes.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Arixmethes is subject to summoning sickness after awakening on its entry turn")
    void awakeningOnEntryTurnPreventsTappingForMana() {
        Permanent arixmethes = harness.enterBattlefieldAndReturn(player1, new ArixmethesSlumberingIsle());
        arixmethes.untap();
        arixmethes.setCounterCount(CounterType.SLUMBER, 1);
        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(arixmethes.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a noncreature spell also triggers slumber counter removal")
    void noncreatureSpellRemovesCounter() {
        Permanent arixmethes = harness.enterBattlefieldAndReturn(player1, new ArixmethesSlumberingIsle());
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(5);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(4);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Casting Arixmethes does not trigger its own counter removal ability")
    void castingArixmethesDoesNotTriggerItself() {
        harness.castFromHand(player1, new ArixmethesSlumberingIsle(), "{2}{G}{U}");
        harness.passBothPriorities();

        Permanent arixmethes = findPermanent(player1, "Arixmethes, Slumbering Isle");
        assertThat(arixmethes.isTapped()).isTrue();
        assertThat(arixmethes.getCounterCount(CounterType.SLUMBER)).isEqualTo(5);
        assertThat(gqs.isLand(gd, arixmethes)).isTrue();
        assertThat(gqs.isCreature(gd, arixmethes)).isFalse();
    }
}
