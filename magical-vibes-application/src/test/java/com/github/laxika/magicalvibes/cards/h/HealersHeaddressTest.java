package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HealersHeaddress.class, Arachnoid.class, DrossCrocodile.class})
class HealersHeaddressTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+2")
    void equippedCreatureGetsToughnessBoost() {
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        Permanent headdress = addHeaddress(player1);
        headdress.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    @DisplayName("The white ability attaches Healer's Headdress to a creature you control")
    void whiteAbilityAttachesToControlledCreature() {
        Permanent headdress = addHeaddress(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(player1, headdress), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(headdress.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The equip ability attaches Healer's Headdress to a creature you control")
    void equipAbilityAttachesToControlledCreature() {
        Permanent headdress = addHeaddress(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, headdress), 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(headdress.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The equipped creature can prevent the next damage to a player")
    void equippedCreaturePreventsNextDamageToPlayer() {
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        Permanent headdress = addHeaddress(player1);
        headdress.setAttachedTo(creature.getId());
        Permanent attacker = addCreatureReady(player2, new DrossCrocodile());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, indexOf(player1, creature), null, player1.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(indexOf(player2, attacker)));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The equipped creature can prevent the next damage to a creature")
    void equippedCreaturePreventsNextDamageToCreature() {
        Permanent abilitySource = addCreatureReady(player1, new Arachnoid());
        Permanent headdress = addHeaddress(player1);
        headdress.setAttachedTo(abilitySource.getId());
        Permanent protectedCreature = addCreatureReady(player1, new Arachnoid());
        Permanent attacker = addCreatureReady(player2, new DrossCrocodile());

        harness.activateAbility(player1, indexOf(player1, abilitySource), null, protectedCreature.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(indexOf(player2, attacker)));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, protectedCreature), indexOf(player2, attacker))));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Both attachment abilities require a creature you control")
    void attachmentAbilitiesCannotTargetOpponentCreature() {
        Permanent headdress = addHeaddress(player1);
        Permanent opponentCreature = addCreatureReady(player2, new Arachnoid());

        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, headdress), 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, headdress), 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Both attachment abilities require sorcery speed")
    void attachmentAbilitiesRequireSorcerySpeed() {
        Permanent headdress = addHeaddress(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, headdress), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, headdress), 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHeaddress(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HealersHeaddress());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
