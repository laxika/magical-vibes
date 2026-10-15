package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LazierGoblin.class, GrizzlyBears.class, ControlMagic.class})
class LazierGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield deals 2 damage to any target")
    void entersAndDealsDamage() {
        harness.setHand(player1, List.of(new LazierGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot attack or block before being motivated")
    void cannotAttackOrBlockBeforeMotivation() {
        Permanent goblin = addCreatureReady(player1, new LazierGoblin());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(goblin.isAttacking()).isFalse();

        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(goblin.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Paying motivate once allows the Goblin to attack")
    void motivationAllowsAttacking() {
        Permanent goblin = addCreatureReady(player1, new LazierGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(goblin.isMotivated()).isTrue();
        declareAttackers(player1, List.of(0));

        assertThat(gd.creaturesAttackedCountThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Paying motivate is immediate and does not use the stack")
    void motivateIsASpecialAction() {
        Permanent goblin = addCreatureReady(player1, new LazierGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(goblin.isMotivated()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Entering the battlefield can deal lethal damage to a creature")
    void entersAndDealsDamageToCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LazierGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A new controller must pay their own motivate cost before blocking")
    void motivationDoesNotTransferToNewController() {
        Permanent goblin = addCreatureReady(player1, new LazierGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, goblin.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(goblin);

        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        int goblinIndex = gd.playerBattlefields.get(player2.getId()).indexOf(goblin);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(goblinIndex, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(goblin.isBlocking()).isFalse();
    }
}
