package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashFloodTest extends BaseCardTest {

    @Test
    void destroysTargetRedPermanent() {
        harness.addToBattlefield(player2, new GoblinRaider());
        harness.setHand(player1, List.of(new FlashFlood()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Goblin Raider"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Raider");
        harness.assertInGraveyard(player2, "Goblin Raider");
    }

    @Test
    void returnsTargetMountainToItsOwnersHand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new FlashFlood()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Mountain"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInHand(player2, "Mountain");
    }

    @Test
    void rejectsTargetsThatDoNotMatchTheChosenMode() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new FlashFlood()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, 0, harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red permanent");
    }
}
