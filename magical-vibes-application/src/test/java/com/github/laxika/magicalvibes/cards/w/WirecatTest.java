package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WirecatTest extends BaseCardTest {

    @Test
    @DisplayName("Wirecat can attack when no enchantment is on the battlefield")
    void canAttackWithoutEnchantment() {
        Permanent wirecat = addReadyWirecat(player1);
        addReadyCreature(player2);
        declareAttackers(player1, List.of(0));

        assertThat(wirecat.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Wirecat can block when no enchantment is on the battlefield")
    void canBlockWithoutEnchantment() {
        Permanent attacker = addReadyCreature(player2);
        attacker.setAttacking(true);
        Permanent wirecat = addReadyWirecat(player1);
        prepareDeclareBlockers(player2);

        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        int wirecatIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wirecat);
        assertThatCode(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(wirecatIndex, attackerIndex))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Wirecat cannot attack while either player controls an enchantment")
    void cannotAttackWhileEnchantmentIsOnBattlefield() {
        addReadyWirecat(player1);
        addEnchantment(player2);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wirecat cannot block while either player controls an enchantment")
    void cannotBlockWhileEnchantmentIsOnBattlefield() {
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        addReadyWirecat(player2);
        addEnchantment(player1);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wirecat can attack again after the enchantment leaves the battlefield")
    void restrictionLiftsWhenEnchantmentLeavesBattlefield() {
        Permanent wirecat = addReadyWirecat(player1);
        addReadyCreature(player2);
        Permanent enchantment = addEnchantment(player2);
        gd.playerBattlefields.get(player2.getId()).remove(enchantment);

        declareAttackers(player1, List.of(0));

        assertThat(wirecat.isAttacking()).isTrue();
    }

    private Permanent addReadyWirecat(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new Wirecat());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setPower(2);
        creature.setToughness(2);
        return addCreatureReady(player, creature);
    }

    private Permanent addEnchantment(com.github.laxika.magicalvibes.model.Player player) {
        Card enchantment = new Card();
        enchantment.setName("Test Enchantment");
        enchantment.setType(CardType.ENCHANTMENT);
        return harness.addToBattlefieldAndReturn(player, enchantment);
    }
}
