package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BattlefieldPercherTest extends BaseCardTest {

    @Test
    void canBlockCreatureWithFlying() {
        Permanent percher = addReadyPercher(player2);
        addAttackingCreature(player1, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(percher.isBlocking()).isTrue();
    }

    @Test
    void cannotBlockCreatureWithoutFlying() {
        addReadyPercher(player2);
        addAttackingCreature(player1, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent percher = addReadyPercher(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(percher.getEffectivePower()).isEqualTo(3);
        assertThat(percher.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(percher.getEffectivePower()).isEqualTo(2);
        assertThat(percher.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyPercher(Player player) {
        Permanent percher = new Permanent(new BattlefieldPercher());
        percher.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(percher);
        return percher;
    }

    private void addAttackingCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
    }
}
