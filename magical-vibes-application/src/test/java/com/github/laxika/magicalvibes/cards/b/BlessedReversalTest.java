package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlessedReversalTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blessed Reversal puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BlessedReversal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Gains 3 life for each creature attacking you")
    void gainsThreeLifePerAttacker() {
        addAttacker(player2, player1.getId());
        addAttacker(player2, player1.getId());

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BlessedReversal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Gains no life when no creatures are attacking you")
    void gainsNoLifeWhenNoAttackers() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BlessedReversal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only counts attackers whose target is you, not other targets")
    void ignoresAttackersTargetingSomethingElse() {
        addAttacker(player2, player1.getId());          // attacking you -> counted
        addAttacker(player2, UUID.randomUUID());        // attacking a planeswalker/other -> not counted

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BlessedReversal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    private Permanent addAttacker(Player player, UUID attackTarget) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(attackTarget);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
