package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
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

@CardUsed({SnowFortress.class, BalduvianBears.class, KjeldoranSkyknight.class})
class SnowFortressTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives +1/+0 until end of turn")
    void pumpsPower() {
        Permanent fortress = addCreatureReady(player1, new SnowFortress());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fortress)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fortress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Second ability gives +0/+1 until end of turn")
    void pumpsToughness() {
        Permanent fortress = addCreatureReady(player1, new SnowFortress());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fortress)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, fortress)).isEqualTo(5);
    }

    @Test
    @DisplayName("Third ability deals 1 damage to an attacking creature without flying")
    void damagesAttacker() {
        addCreatureReady(player1, new SnowFortress());
        Permanent attacker = addAttacker(player2, player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetFlyer() {
        addCreatureReady(player1, new SnowFortress());
        Permanent flyer = addAttacker(player2, player1, new KjeldoranSkyknight());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking you")
    void cannotTargetNonAttacker() {
        addCreatureReady(player1, new SnowFortress());
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Defender prevents Snow Fortress from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new SnowFortress());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Third ability does nothing if the target stops attacking before resolution")
    void doesNotDamageTargetThatStopsAttacking() {
        addCreatureReady(player1, new SnowFortress());
        Permanent attacker = addAttacker(player2, player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = addCreatureReady(controller, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
