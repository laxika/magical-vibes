package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed(RagingGoblin.class)
class RagingGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as CREATURE_SPELL")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new RagingGoblin(), "{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(RagingGoblin.class);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new RagingGoblin()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Raging Goblin onto the battlefield with haste")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(goblin.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can attack the turn it enters the battlefield due to haste")
    void canAttackWithSummoningSicknessDueToHaste() {
        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent goblin = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(goblin.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}

