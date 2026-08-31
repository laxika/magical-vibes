package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Brainwash.class, FountainOfYouth.class, GrizzlyBears.class})
class BrainwashTest extends BaseCardTest {

    private void enchant(Permanent creature, Player controller) {
        Permanent brainwash = harness.addToBattlefieldAndReturn(controller, new Brainwash());
        brainwash.setAttachedTo(creature.getId());
    }

    @Test
    @DisplayName("Enchanted creature can attack when its controller pays {3}")
    void attacksWhenPaid() {
        harness.setLife(player2, 20);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears, player2);

        harness.addMana(player1, ManaColor.WHITE, 3);
        declareAttackers(List.of(0));

        // {3} tax consumed and the 2/2 connected (no blockers)
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Enchanted creature can't attack when its controller can't pay {3}")
    void cannotAttackWithoutPayment() {
        harness.setLife(player2, 20);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears, player2);

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(bears.isAttacking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the enchanted creature is taxed; another creature attacks for free")
    void otherCreaturesUnaffected() {
        harness.setLife(player2, 20);
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        enchant(enchanted, player2);

        Permanent free = addCreatureReady(player1, new GrizzlyBears());

        int freeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(free);
        // No mana available, yet the un-enchanted creature attacks and connects for 2
        declareAttackers(List.of(freeIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Brainwash taxes only attacking — the enchanted creature blocks for free")
    void blockingWithTheEnchantedCreatureIsFree() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        enchant(blocker, player1);

        prepareDeclareBlockers();

        // player2's pool is empty: reading the ATTACK tax as BLOCK_WITH would reject this block
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Brainwash taxes only attacking — blocking the enchanted creature is free")
    void blockingTheEnchantedCreatureIsFree() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        enchanted.setAttacking(true);
        enchant(enchanted, player2);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        // player2's pool is empty: reading the ATTACK tax as BE_BLOCKED_BY would reject this block
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(enchanted))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void enchantsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Brainwash brainwash = new Brainwash();
        harness.setHand(player1, List.of(brainwash));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(brainwash.getId())
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void cannotEnchantNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Brainwash()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
