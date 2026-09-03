package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GhostShip;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Flood.class, Squire.class, GhostShip.class})
class FloodTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature without flying")
    void resolvingTapsNonFlyingCreature() {
        harness.addToBattlefieldAndReturn(player1, new Flood());
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent target = addCreatureReady(player2, new Squire());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyingCreature() {
        harness.addToBattlefieldAndReturn(player1, new Flood());
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent flyer = addCreatureReady(player2, new GhostShip());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new Flood());
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Flood());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void canTargetTappedCreature() {
        harness.addToBattlefieldAndReturn(player1, new Flood());
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent target = addCreatureReady(player2, new Squire());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }
}
