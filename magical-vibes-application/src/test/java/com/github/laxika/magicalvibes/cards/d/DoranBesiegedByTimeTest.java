package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoranBesiegedByTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells with greater toughness cost {1} less to cast")
    void reducesCreatureSpellsWithGreaterToughness() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creature spells without greater toughness are not reduced")
    void doesNotReduceCreatureSpellsWithoutGreaterToughness() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        harness.setHand(player1, List.of(new GoblinPiker()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacking creature you control gets +X/+X for its power-toughness difference")
    void boostsAttackingCreatureYouControl() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        Permanent attacker = addReadyCreature(player1, new GiantSpider());

        declareDoranAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("A blocking creature you control gets +X/+X for its power-toughness difference")
    void boostsBlockingCreatureYouControl() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        Permanent blocker = addReadyCreature(player1, new GiantSpider());
        Permanent attacker = addReadyCreature(player2, new GoblinPiker());
        attacker.setAttacking(true);

        declareBlockers(List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature controlled by an opponent does not trigger Doran when it blocks")
    void opponentBlockingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new DoranBesiegedByTime());
        Permanent attacker = addReadyCreature(player1, new GoblinPiker());
        Permanent blocker = addReadyCreature(player2, new GiantSpider());
        attacker.setAttacking(true);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareDoranAttackers(Player attackingPlayer, List<Integer> attackerIndexes) {
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, attackingPlayer, attackerIndexes);
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, assignments);
    }
}
