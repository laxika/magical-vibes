package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzoriusPloy.class, GrizzlyBears.class, Shock.class, Forest.class})
class AzoriusPloyTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage dealt to and by the target creature")
    void preventsCombatDamageToAndByTargetCreature() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        Permanent blocker = addBlocker(player2, 3, 3, 0);

        castAzoriusPloy(attacker);
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to the target creature")
    void doesNotPreventNoncombatDamage() {
        Permanent target = addCreature(player1, 3, 3);
        castAzoriusPloy(target);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AzoriusPloy()));
        addAzoriusPloyMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAzoriusPloy(Permanent target) {
        harness.setHand(player1, List.of(new AzoriusPloy()));
        addAzoriusPloyMana(player1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addAzoriusPloyMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent addCreature(Player owner, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        return addCreatureReady(owner, bears);
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Permanent attacker = addCreature(owner, power, toughness);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Permanent blocker = addCreature(owner, power, toughness);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockedAttackerIndex);
        return blocker;
    }
}
