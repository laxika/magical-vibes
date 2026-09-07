package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrustyBoomerang.class, GrizzlyBears.class, Forest.class})
class TrustyBoomerangTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Trusty Boomerang to target creature")
    void equipAttachesToCreature() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boomerang.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature taps a target creature and returns Trusty Boomerang to its owner's hand")
    void tapsCreatureAndReturnsBoomerangToHand() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(boomerang.getAttachedTo()).isNull();
        harness.assertInHand(player1, "Trusty Boomerang");
        harness.assertNotOnBattlefield(player1, "Trusty Boomerang");
    }

    @Test
    @DisplayName("Granted ability cannot target a land")
    void cannotTargetLand() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
