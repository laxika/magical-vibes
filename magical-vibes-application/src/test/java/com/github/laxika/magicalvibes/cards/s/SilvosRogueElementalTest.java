package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilvosRogueElemental.class, GrizzlyBears.class})
class SilvosRogueElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Silvos's regeneration ability grants a regeneration shield")
    void resolvingRegenerationAbilityGrantsShield() {
        Permanent silvos = addSilvosReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(silvos.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Silvos's regeneration ability costs one green mana and does not tap it")
    void regenerationAbilityCostsGreenManaWithoutTapping() {
        Permanent silvos = addSilvosReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(silvos.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Silvos cannot activate regeneration without green mana")
    void cannotActivateRegenerationWithoutMana() {
        addSilvosReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A regeneration shield saves Silvos from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent silvos = addSilvosReady(player1);
        silvos.setRegenerationShield(1);
        silvos.setBlocking(true);
        silvos.addBlockingTarget(0);

        GrizzlyBears attackerCard = new GrizzlyBears();
        attackerCard.setPower(6);
        attackerCard.setToughness(6);
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(silvos);
        assertThat(silvos.isTapped()).isTrue();
        assertThat(silvos.getRegenerationShield()).isZero();
    }

    private Permanent addSilvosReady(Player player) {
        Permanent silvos = harness.addToBattlefieldAndReturn(player, new SilvosRogueElemental());
        silvos.setSummoningSick(false);
        return silvos;
    }
}
