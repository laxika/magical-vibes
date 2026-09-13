package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinBerserker;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WakeOfDestruction.class, YavimayaHollow.class, GoblinBerserker.class})
class WakeOfDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target land and every other land with the same name")
    void destroysTargetAndAllSameNameLands() {
        harness.addToBattlefield(player2, new YavimayaHollow());
        harness.addToBattlefield(player1, new YavimayaHollow());
        harness.addToBattlefield(player2, new GoblinBerserker());

        UUID targetId = harness.getPermanentId(player2, "Yavimaya Hollow");
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yavimaya Hollow");
        harness.assertNotOnBattlefield(player2, "Yavimaya Hollow");
        harness.assertOnBattlefield(player2, "Goblin Berserker");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GoblinBerserker());
        harness.addToBattlefield(player2, new YavimayaHollow());
        harness.setHand(player1, List.of(new WakeOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Goblin Berserker");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
