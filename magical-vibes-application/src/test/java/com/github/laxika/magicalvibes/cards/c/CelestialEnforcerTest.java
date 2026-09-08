package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CelestialEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature when you control a creature with flying")
    void tapsTargetCreatureWithControlledFlyer() {
        Permanent enforcer = addReadyEnforcer(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new SuntailHawk());
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
        Permanent enforcer = addReadyEnforcer(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent enforcer = addReadyEnforcer(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.f.Forest());
        addCreatureReady(player1, new SuntailHawk());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(enforcer), null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEnforcer(Player player) {
        Permanent permanent = new Permanent(new CelestialEnforcer());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
