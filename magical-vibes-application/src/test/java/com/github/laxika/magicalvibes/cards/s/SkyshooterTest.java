package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkyshooterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices Skyshooter and destroys an attacking creature with flying")
    void destroysAttackingFlyingCreature() {
        addReadySkyshooter(player1);
        Permanent target = addCombatCreature(player2, new SuntailHawk(), true, false);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Skyshooter");
        harness.assertInGraveyard(player1, "Skyshooter");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Destroys a blocking creature with flying")
    void destroysBlockingFlyingCreature() {
        addReadySkyshooter(player1);
        Permanent target = addCombatCreature(player2, new SuntailHawk(), false, true);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addReadySkyshooter(player1);
        Permanent target = addCombatCreature(player2, new GrizzlyBears(), true, false);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a flying creature that is not attacking or blocking")
    void cannotTargetIdleFlyingCreature() {
        addReadySkyshooter(player1);
        Permanent target = addCombatCreature(player2, new SuntailHawk(), false, false);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadySkyshooter(Player player) {
        Permanent permanent = new Permanent(new Skyshooter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private Permanent addCombatCreature(Player player, Card card, boolean attacking, boolean blocking) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(attacking);
        permanent.setBlocking(blocking);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
