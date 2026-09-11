package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrepidHero.class, ThunderingGiant.class, ArgothianSwine.class, Forest.class})
class IntrepidHeroTest extends BaseCardTest {

    private Permanent setup() {
        return addCreatureReady(player1, new IntrepidHero());
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    @Test
    @DisplayName("Destroys a creature with power 4 or greater")
    void destroysHighPowerCreature() {
        Permanent hero = setup();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new ThunderingGiant());

        harness.activateAbility(player1, idxOf(hero), 0, null, giant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Thundering Giant");
        harness.assertInGraveyard(player2, "Thundering Giant");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        Permanent hero = setup();
        Permanent swine = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, swine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent hero = setup();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps itself as the activation cost")
    void tapsItselfAsActivationCost() {
        Permanent hero = setup();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new ThunderingGiant());

        harness.activateAbility(player1, idxOf(hero), 0, null, giant.getId());

        assertThat(hero.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Thundering Giant");
    }

    @Test
    @DisplayName("Does not destroy a target that loses the required power before resolution")
    void doesNotDestroyTargetThatLosesRequiredPowerBeforeResolution() {
        Permanent hero = setup();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new ThunderingGiant());

        harness.activateAbility(player1, idxOf(hero), 0, null, giant.getId());
        giant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Thundering Giant");
        harness.assertNotInGraveyard(player2, "Thundering Giant");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent hero = setup();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
