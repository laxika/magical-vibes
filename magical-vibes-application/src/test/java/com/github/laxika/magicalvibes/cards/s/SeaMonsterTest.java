package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaMonster.class, Island.class})
class SeaMonsterTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Sea Monster puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SeaMonster()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(SeaMonster.class);
    }

    @Test
    @DisplayName("Resolving puts Sea Monster onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SeaMonster()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard())
                .isInstanceOf(SeaMonster.class);
    }

    @Test
    @DisplayName("Sea Monster enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new SeaMonster()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Sea Monster can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new SeaMonster());

        declareAttackers(List.of(0));

        // Combat auto-advances; verify attack went through by checking damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Sea Monster cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new SeaMonster());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sea Monster cannot attack when only its controller controls an Island")
    void cannotAttackWhenOnlyAttackerControlsIsland() {
        addCreatureReady(player1, new SeaMonster());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sea Monster cannot attack if defender controls only a changeling creature")
    @CardUsed(AvianChangeling.class)
    void cannotAttackWhenDefenderOnlyControlsChangelingCreature() {
        harness.addToBattlefield(player2, new AvianChangeling());
        addCreatureReady(player1, new SeaMonster());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Unblocked Sea Monster deals 6 damage to defending player")
    void dealsSixDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent seaPerm = addCreatureReady(player1, new SeaMonster());
        seaPerm.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}

