package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardiansOfOboro.class, WallOfStone.class})
class GuardiansOfOboroTest extends BaseCardTest {

    @Test
    @DisplayName("An unmodified creature with defender cannot attack")
    void unmodifiedCreatureCannotAttack() {
        harness.addToBattlefield(player1, new GuardiansOfOboro());
        Permanent wall = addCreatureReady(player1, new WallOfStone());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A modified creature you control can attack as though it had no defender")
    void modifiedCreatureYouControlCanAttack() {
        harness.addToBattlefield(player1, new GuardiansOfOboro());
        Permanent wall = addCreatureReady(player1, new WallOfStone());
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new WallOfStone());

        declareAttackers(List.of(1));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A modified creature controlled by an opponent cannot use Guardians of Oboro")
    void opponentModifiedCreatureCannotAttack() {
        harness.addToBattlefield(player1, new GuardiansOfOboro());
        Permanent wall = addCreatureReady(player2, new WallOfStone());
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
