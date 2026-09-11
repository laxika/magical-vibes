package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FlowstoneShambler;
import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalRage.class, FlowstoneShambler.class, SkyshroudFalcon.class})
class PrimalRageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Primal Rage puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PrimalRage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(PrimalRage.class);
    }

    @Test
    @DisplayName("Creatures you control gain trample")
    void ownCreaturesGainTrample() {
        Permanent creature = addCreatureReady(player1, new FlowstoneShambler());
        harness.addToBattlefield(player1, new PrimalRage());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain trample")
    void opponentCreaturesDoNotGainTrample() {
        Permanent opponentCreature = addCreatureReady(player2, new FlowstoneShambler());
        harness.addToBattlefield(player1, new PrimalRage());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering under your control gain trample")
    void creaturesEnteringAfterSourceGainTrample() {
        harness.addToBattlefield(player1, new PrimalRage());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new FlowstoneShambler());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample bonus is removed when Primal Rage leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent creature = addCreatureReady(player1, new FlowstoneShambler());
        Permanent rage = harness.addToBattlefieldAndReturn(player1, new PrimalRage());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(rage);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted trample allows excess combat damage to defending player")
    void grantedTrampleWorksInCombat() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PrimalRage());

        Permanent attacker = addCreatureReady(player1, new FlowstoneShambler());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 2/2 trample blocked by 1/1 → assign lethal to blocker, excess to player
        harness.handleCombatDamageAssigned(player1, 1, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
