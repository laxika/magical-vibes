package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({RagingGoblin.class})
class RagingGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as CREATURE_SPELL")
    void castingPutsOnStack() {
        Card card = new RagingGoblin();
        harness.castFromHand(player1, card, "{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isSameAs(card);
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
    @DisplayName("Resolving puts Raging Goblin onto the battlefield")
    void resolvingPutsOnBattlefield() {
        Card card = new RagingGoblin();
        harness.castFromHand(player1, card, "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == card);
    }

    @Test
    @DisplayName("Can attack the turn it enters the battlefield due to haste")
    void canAttackWithSummoningSicknessDueToHaste() {
        Card card = new RagingGoblin();
        harness.castFromHand(player1, card, "{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent goblin = findPermanent(player1, "Raging Goblin");
        assertThat(goblin.getOriginalCard()).isSameAs(card);
        assertThat(goblin.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Haste does not allow a tapped creature to attack")
    void cannotAttackWhileTappedDespiteHaste() {
        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        harness.passBothPriorities();
        Permanent goblin = findPermanent(player1, "Raging Goblin");
        goblin.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
