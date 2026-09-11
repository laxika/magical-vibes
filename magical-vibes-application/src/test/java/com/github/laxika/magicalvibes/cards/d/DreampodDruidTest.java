package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreampodDruid.class, Pacifism.class})
class DreampodDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a Saproling while it is not enchanted")
    void doesNotCreateTokenWhenNotEnchanted() {
        harness.addToBattlefield(player1, new DreampodDruid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(saprolingCount(player1)).isZero();
    }

    @Test
    @DisplayName("Creates a Saproling during each upkeep while enchanted")
    void createsTokenDuringEachUpkeepWhenEnchanted() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DreampodDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(druid.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(saprolingCount(player1)).isEqualTo(1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(saprolingCount(player1)).isEqualTo(2);
    }

    private long saprolingCount(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Saproling".equals(permanent.getCard().getName()))
                .count();
    }
}
