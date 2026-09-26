package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TrustedAdvisor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CowedByWisdom.class, TrustedAdvisor.class})
class CowedByWisdomTest extends BaseCardTest {

    @Test
    void enchantedCreatureCanAttackWhenItsControllerPaysForAuraControllersHand() {
        Permanent creature = addCreatureReady(player1, new TrustedAdvisor());
        enchant(creature, player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new TrustedAdvisor(), new TrustedAdvisor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void enchantedCreatureCannotAttackWithoutEnoughMana() {
        Permanent creature = addCreatureReady(player1, new TrustedAdvisor());
        enchant(creature, player2);
        harness.setHand(player2, List.of(new TrustedAdvisor(), new TrustedAdvisor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(creature))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantedCreatureCanBlockWhenItsControllerPaysForAuraControllersHand() {
        Permanent attacker = addCreatureReady(player1, new TrustedAdvisor());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TrustedAdvisor());
        enchant(blocker, player1);
        harness.setHand(player1, List.of(new TrustedAdvisor(), new TrustedAdvisor()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void enchantedCreatureCannotBlockWithoutEnoughMana() {
        Permanent attacker = addCreatureReady(player1, new TrustedAdvisor());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TrustedAdvisor());
        enchant(blocker, player1);
        harness.setHand(player1, List.of(new TrustedAdvisor(), new TrustedAdvisor()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    private void enchant(Permanent creature, Player auraController) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new CowedByWisdom());
        aura.setAttachedTo(creature.getId());
    }
}
