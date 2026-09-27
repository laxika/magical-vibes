package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelJiladExile.class, AlphaMyr.class})
class TelJiladExileTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        Permanent exile = addTelJiladExileReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(exile.getRegenerationShield()).isEqualTo(1);
        assertThat(exile.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The regeneration ability requires its generic mana component")
    void regenerationAbilityRequiresGenericMana() {
        addTelJiladExileReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The regeneration ability requires green mana")
    void regenerationAbilityRequiresGreenMana() {
        addTelJiladExileReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A regeneration shield saves Tel-Jilad Exile from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent exile = addTelJiladExileReady(player1);
        exile.setRegenerationShield(1);
        exile.setBlocking(true);
        exile.addBlockingTarget(0);

        AlphaMyr attackerCard = new AlphaMyr();
        attackerCard.setPower(3);
        Permanent attacker = addCreatureReady(player2, attackerCard);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Tel-Jilad Exile");
        Permanent survivingExile = findPermanent(player1, "Tel-Jilad Exile");
        assertThat(survivingExile.isTapped()).isTrue();
        assertThat(survivingExile.getRegenerationShield()).isZero();
    }

    private Permanent addTelJiladExileReady(Player player) {
        return addCreatureReady(player, new TelJiladExile());
    }
}
