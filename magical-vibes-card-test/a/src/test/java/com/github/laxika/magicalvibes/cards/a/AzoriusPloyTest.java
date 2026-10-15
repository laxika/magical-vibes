package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CacklingFlames;
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

@CardUsed({AzoriusPloy.class, AzoriusFirstWing.class, CacklingFlames.class, AzoriusSignet.class})
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
    @DisplayName("Prevents combat damage dealt by the target creature to a player")
    void preventsCombatDamageDealtByTargetCreatureToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2, 2, 2);

        castAzoriusPloy(attacker);
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("Does not prevent noncombat damage to the target creature")
    void doesNotPreventNoncombatDamage() {
        Permanent target = addCreature(player1, 4, 4);
        castAzoriusPloy(target);

        harness.setHand(player2, List.of(new CacklingFlames(), new AzoriusSignet()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("First target cannot deal combat damage and second target cannot receive it")
    void preventsDamageInSeparateDirectionsForDifferentTargets() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        Permanent blocker = addBlocker(player2, 3, 3, 0);
        harness.setHand(player1, List.of(new AzoriusPloy()));
        addAzoriusPloyMana(player1);

        harness.castAndResolveInstant(player1, 0, List.of(blocker.getId(), attacker.getId()));
        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Both target choices are required even when choosing the same creature")
    void cannotCastWithOnlyOneTargetChoice() {
        Permanent target = addCreature(player1, 2, 2);
        harness.setHand(player1, List.of(new AzoriusPloy()));
        addAzoriusPloyMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.setHand(player1, List.of(new AzoriusPloy()));
        addAzoriusPloyMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(signet.getId(), signet.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAzoriusPloy(Permanent target) {
        harness.setHand(player1, List.of(new AzoriusPloy()));
        addAzoriusPloyMana(player1);
        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), target.getId()));
    }

    private void addAzoriusPloyMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent addCreature(Player owner, int power, int toughness) {
        Card creature = new AzoriusFirstWing();
        creature.setPower(power);
        creature.setToughness(toughness);
        return addCreatureReady(owner, creature);
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
