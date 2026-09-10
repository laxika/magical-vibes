package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhipgrassEntangler.class, GrizzlyBears.class})
class WhipgrassEntanglerTest extends BaseCardTest {

    @Test
    @DisplayName("The ability makes a creature pay for each Cleric when attacking")
    void attackTaxCountsClericsAtDeclaration() {
        Permanent entangler = addCreatureReady(player1, new WhipgrassEntangler());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        addCreatureReady(player1, new WhipgrassEntangler());
        assertThatThrownBy(() -> declareAttackers(player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(player1, List.of(1));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(entangler.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The ability also taxes blocking")
    void blockTaxIsPaidByBlockerController() {
        Permanent entangler = addCreatureReady(player1, new WhipgrassEntangler());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(entangler.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a player")
    void rejectsPlayerTarget() {
        addCreatureReady(player1, new WhipgrassEntangler());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
