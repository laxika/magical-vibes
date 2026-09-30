package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MalachOfTheDawn.class)
class MalachOfTheDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {W}{W}{W} grants a regeneration shield")
    void activationGrantsRegenerationShield() {
        Permanent malach = addCreatureReady(player1, new MalachOfTheDawn());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(malach.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability requires three white mana")
    void activationRequiresThreeWhiteMana() {
        Permanent malach = addCreatureReady(player1, new MalachOfTheDawn());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(malach.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A regeneration shield saves Malach of the Dawn from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent malach = addCreatureReady(player1, new MalachOfTheDawn());
        malach.setRegenerationShield(1);
        malach.setBlocking(true);
        malach.addBlockingTarget(0);

        MalachOfTheDawn attackerCard = new MalachOfTheDawn();
        attackerCard.setPower(5);
        attackerCard.setToughness(5);
        Permanent attacker = addCreatureReady(player2, attackerCard);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Malach of the Dawn");
        assertThat(malach.isTapped()).isTrue();
        assertThat(malach.getRegenerationShield()).isZero();
        assertThat(malach.getMarkedDamage()).isZero();
    }
}
