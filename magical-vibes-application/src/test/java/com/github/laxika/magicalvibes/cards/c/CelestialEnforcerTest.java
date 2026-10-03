package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrowsingTyrannodon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestialEnforcer.class, DrowsingTyrannodon.class, ConcordiaPegasus.class, Forest.class})
class CelestialEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature when you control a creature with flying")
    void tapsTargetCreatureWithControlledFlyer() {
        Permanent enforcer = addCreatureReady(player1, new CelestialEnforcer());
        Permanent target = addCreatureReady(player2, new DrowsingTyrannodon());
        addCreatureReady(player1, new ConcordiaPegasus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(enforcer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(enforcer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without controlling a creature with flying")
    void cannotActivateWithoutControlledFlyer() {
        Permanent enforcer = addCreatureReady(player1, new CelestialEnforcer());
        Permanent target = addCreatureReady(player2, new DrowsingTyrannodon());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enforcer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with flying");
        assertThat(enforcer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent enforcer = addCreatureReady(player1, new CelestialEnforcer());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addCreatureReady(player1, new ConcordiaPegasus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enforcer), null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsFlyerDoesNotEnableActivation() {
        Permanent enforcer = addCreatureReady(player1, new CelestialEnforcer());
        Permanent target = addCreatureReady(player2, new ConcordiaPegasus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enforcer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures with flying");
        assertThat(enforcer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void tappedSummoningSickFlyerEnablesActivationAndCanBeTargeted() {
        Permanent enforcer = addCreatureReady(player1, new CelestialEnforcer());
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new ConcordiaPegasus());
        flyer.setTapped(true);
        flyer.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(enforcer), null, flyer.getId());
        harness.passBothPriorities();

        assertThat(enforcer.isTapped()).isTrue();
        assertThat(flyer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterLastControlledFlyerLeavesBattlefield() {
        Permanent enforcer = addCreatureReady(player1, new CelestialEnforcer());
        Permanent target = addCreatureReady(player2, new DrowsingTyrannodon());
        Permanent flyer = addCreatureReady(player1, new ConcordiaPegasus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(enforcer), null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(flyer);
        gd.playerGraveyards.get(player1.getId()).add(flyer.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTapOwnUntappedCreature() {
        Permanent enforcer = addCreatureReady(player1, new CelestialEnforcer());
        Permanent target = addCreatureReady(player1, new ConcordiaPegasus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(enforcer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new CelestialEnforcer());
        enforcer.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new DrowsingTyrannodon());
        addCreatureReady(player1, new ConcordiaPegasus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enforcer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enforcer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        Permanent enforcer = addCreatureReady(player1, new CelestialEnforcer());
        Permanent target = addCreatureReady(player2, new DrowsingTyrannodon());
        addCreatureReady(player1, new ConcordiaPegasus());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enforcer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enforcer.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
