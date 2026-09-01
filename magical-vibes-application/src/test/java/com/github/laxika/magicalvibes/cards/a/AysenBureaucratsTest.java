package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.n.Narwhal;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AysenBureaucrats.class, Narwhal.class, AbbeyGargoyles.class, AysenAbbey.class,
        AysenCrusader.class, BeastWalkers.class})
class AysenBureaucratsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature with power 2 or less")
    void resolvingTapsLowPowerCreature() {
        addCreatureReady(player1, new AysenBureaucrats());
        Permanent target = addCreatureReady(player2, new Narwhal());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps the Bureaucrats")
    void activatingTapsSelf() {
        Permanent bureaucrats = addCreatureReady(player1, new AysenBureaucrats());
        Permanent target = addCreatureReady(player2, new Narwhal());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(bureaucrats.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player1, new AysenBureaucrats());
        Permanent giant = addCreatureReady(player2, new AbbeyGargoyles());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new AysenBureaucrats());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AysenAbbey());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target must still have power 2 or less when the ability resolves")
    void targetBecomingTooPowerfulMakesAbilityFizzle() {
        addCreatureReady(player1, new AysenBureaucrats());
        Permanent target = addCreatureReady(player2, new AysenCrusader());

        harness.activateAbility(player1, 0, null, target.getId());
        addCreatureReady(player2, new BeastWalkers());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }
}
