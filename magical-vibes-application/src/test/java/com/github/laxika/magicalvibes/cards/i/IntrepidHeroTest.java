package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, Forest.class, HillGiant.class, HowlingMine.class, IntrepidHero.class})
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
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.activateAbility(player1, idxOf(hero), 0, null, elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Can destroy a high-power creature it controls")
    void destroysOwnHighPowerCreature() {
        Permanent hero = setup();
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        harness.activateAbility(player1, idxOf(hero), 0, null, elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        Permanent hero = setup();
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, hillGiant.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hero.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        Permanent hero = setup();
        Permanent mine = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, mine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent hero = setup();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(hero), 0, null, player2.getId()))
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
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.activateAbility(player1, idxOf(hero), 0, null, elemental.getId());

        assertThat(hero.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Does not destroy a target that loses the required power before resolution")
    void doesNotDestroyTargetThatLosesRequiredPowerBeforeResolution() {
        Permanent hero = setup();
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.activateAbility(player1, idxOf(hero), 0, null, elemental.getId());
        elemental.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
    }
}
