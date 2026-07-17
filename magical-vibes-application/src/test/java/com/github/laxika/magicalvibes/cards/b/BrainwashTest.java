package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrainwashTest extends BaseCardTest {

    private void enchant(Permanent creature, Player controller) {
        Permanent brainwash = new Permanent(new Brainwash());
        brainwash.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(brainwash);
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("Enchanted creature can attack when its controller pays {3}")
    void attacksWhenPaid() {
        harness.setLife(player2, 20);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        enchant(bears, player2);

        harness.addMana(player1, ManaColor.WHITE, 3);
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of(0));

        // {3} tax consumed and the 2/2 connected (no blockers)
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Enchanted creature can't attack when its controller can't pay {3}")
    void cannotAttackWithoutPayment() {
        harness.setLife(player2, 20);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        enchant(bears, player2);

        harness.addMana(player1, ManaColor.WHITE, 2);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the enchanted creature is taxed; another creature attacks for free")
    void otherCreaturesUnaffected() {
        harness.setLife(player2, 20);
        Permanent enchanted = new Permanent(new GrizzlyBears());
        enchanted.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(enchanted);
        enchant(enchanted, player2);

        Permanent free = new Permanent(new GrizzlyBears());
        free.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(free);

        beginDeclareAttackers();

        int freeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(free);
        // No mana available, yet the un-enchanted creature attacks and connects for 2
        gs.declareAttackers(gd, player1, List.of(freeIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
