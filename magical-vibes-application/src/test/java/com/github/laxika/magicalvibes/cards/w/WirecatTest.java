package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.a.ArcaneLaboratory;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wirecat.class, ArgothianSwine.class, ArcaneLaboratory.class, Humble.class})
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
    @DisplayName("Wirecat cannot attack while its controller controls an enchantment")
    void cannotAttackWhileControllerControlsEnchantment() {
        addReadyWirecat(player1);
        addEnchantment(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wirecat can attack with an enchantment present after it loses its abilities")
    void restrictionDoesNotApplyAfterWirecatLosesAbilities() {
        Permanent wirecat = addReadyWirecat(player1);
        addReadyCreature(player2);
        addEnchantment(player2);

        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, wirecat.getId());

        declareAttackers(player1, List.of(0));

        assertThat(wirecat.isAttacking()).isTrue();
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

    private Permanent addReadyWirecat(Player player) {
        return addCreatureReady(player, new Wirecat());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new ArgothianSwine());
    }

    private Permanent addEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ArcaneLaboratory());
    }
}
