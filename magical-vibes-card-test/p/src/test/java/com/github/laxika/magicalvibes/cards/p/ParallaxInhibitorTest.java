package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.j.JoltingMerfolk;
import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParallaxInhibitor.class, JoltingMerfolk.class, RootwaterCommando.class, ParallaxTide.class})
class ParallaxInhibitorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a fade counter on each fading permanent you control")
    void putsFadeCountersOnControlledFadingPermanents() {
        addCreatureReady(player1, new ParallaxInhibitor());
        Permanent fadingPermanent = addCreatureReady(player1, new JoltingMerfolk());
        Permanent nonFadingPermanent = addCreatureReady(player1, new RootwaterCommando());
        Permanent opponentFadingPermanent = addCreatureReady(player2, new JoltingMerfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fadingPermanent.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(nonFadingPermanent.getCounterCount(CounterType.FADE)).isZero();
        assertThat(opponentFadingPermanent.getCounterCount(CounterType.FADE)).isZero();
        harness.assertNotOnBattlefield(player1, "Parallax Inhibitor");
        harness.assertInGraveyard(player1, "Parallax Inhibitor");
    }

    @Test
    @DisplayName("Requires one generic mana to activate")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new ParallaxInhibitor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Puts a fade counter on every fading permanent you control")
    void putsFadeCounterOnEveryControlledFadingPermanent() {
        addCreatureReady(player1, new ParallaxInhibitor());
        Permanent firstFadingPermanent = addCreatureReady(player1, new JoltingMerfolk());
        Permanent secondFadingPermanent = addCreatureReady(player1, new JoltingMerfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firstFadingPermanent.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(secondFadingPermanent.getCounterCount(CounterType.FADE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate while the artifact is tapped")
    void cannotActivateWhileTapped() {
        Permanent inhibitor = addCreatureReady(player1, new ParallaxInhibitor());
        inhibitor.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices the artifact as a cost before adding to existing fade counters")
    void sacrificesAsCostAndAddsToExistingCounters() {
        addCreatureReady(player1, new ParallaxInhibitor());
        Permanent merfolk = addCreatureReady(player1, new JoltingMerfolk());
        merfolk.setCounterCount(CounterType.FADE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Parallax Inhibitor");
        harness.assertInGraveyard(player1, "Parallax Inhibitor");
        assertThat(merfolk.getCounterCount(CounterType.FADE)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can activate with no fading permanents and affects those present at resolution")
    void checksFadingPermanentsAtResolution() {
        harness.addToBattlefield(player1, new ParallaxInhibitor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new JoltingMerfolk());

        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Parallax Inhibitor");
    }

    @Test
    @DisplayName("Adds fade counters to noncreature permanents with fading")
    void addsCounterToFadingEnchantment() {
        harness.addToBattlefield(player1, new ParallaxInhibitor());
        Permanent tide = harness.addToBattlefieldAndReturn(player1, new ParallaxTide());
        tide.setCounterCount(CounterType.FADE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tide.getCounterCount(CounterType.FADE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Fade counters alone do not make a permanent eligible")
    void doesNotAddCountersToNonFadingPermanentWithFadeCounters() {
        harness.addToBattlefield(player1, new ParallaxInhibitor());
        Permanent commando = addCreatureReady(player1, new RootwaterCommando());
        commando.setCounterCount(CounterType.FADE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(commando.getCounterCount(CounterType.FADE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Parallax Inhibitor");
    }
}
