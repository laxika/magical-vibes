package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EzurisArchers.class, GrizzlyBears.class, SuntailHawk.class, Disperse.class})
class EzurisArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature with flying triggers +3/+0 boost")
    void blockingFlyingCreatureTriggersBoost() {
        Permanent archers = addReadyArchers(player2);
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ezuri's Archers");

        // Resolve the trigger
        harness.passBothPriorities();

        // Archers should have +3/+0
        assertThat(archers.getPowerModifier()).isEqualTo(3);
        assertThat(archers.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(4);   // 1 base + 3
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(2); // 2 base + 0
    }

    @Test
    @DisplayName("Blocking a creature without flying does not trigger boost")
    void blockingNonFlyingCreatureDoesNotTrigger() {
        Permanent archers = addReadyArchers(player2);
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // No trigger should be on the stack
        assertThat(gd.stack).isEmpty();

        // Archers should have no boost
        assertThat(archers.getPowerModifier()).isEqualTo(0);
        assertThat(archers.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent archers = addReadyArchers(player2);
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(archers.getPowerModifier()).isEqualTo(3);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(archers);
        assertThat(archers.getPowerModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the Archers blocking the flyer receives the boost")
    void nonblockingArchersDoesNotReceiveBoost() {
        Permanent blockingArchers = addReadyArchers(player2);
        Permanent idleArchers = addReadyArchers(player2);
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blockingArchers)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, idleArchers)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost still resolves after the blocked flyer leaves the battlefield")
    void boostResolvesAfterFlyerLeavesBattlefield() {
        Permanent archers = addReadyArchers(player2);
        Permanent flyer = addReadyAttacker(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, flyer.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(flyer);
        harness.assertInHand(player1, "Suntail Hawk");
        assertThat(archers.getPowerModifier()).isEqualTo(0);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost does not affect another Archers when its source leaves the battlefield")
    void boostDoesNotTransferWhenSourceLeavesBattlefield() {
        Permanent blockingArchers = addReadyArchers(player2);
        Permanent idleArchers = addReadyArchers(player2);
        addReadyAttacker(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, blockingArchers.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blockingArchers);
        harness.assertInHand(player2, "Ezuri's Archers");

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, idleArchers)).isEqualTo(1);
        assertThat(idleArchers.getPowerModifier()).isEqualTo(0);
    }

    private Permanent addReadyArchers(Player player) {
        return addCreatureReady(player, new EzurisArchers());
    }

    private Permanent addReadyAttacker(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
        return perm;
    }
}
