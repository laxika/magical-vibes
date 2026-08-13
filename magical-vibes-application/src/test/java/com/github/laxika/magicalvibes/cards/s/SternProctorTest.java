package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SternProctorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target artifact to its owner's hand")
    void etbReturnsArtifact() {
        harness.addToBattlefield(player2, new GildedLotus());
        UUID targetId = harness.getPermanentId(player2, "Gilded Lotus");
        castSternProctor(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gilded Lotus");
        harness.assertInHand(player2, "Gilded Lotus");
        harness.assertOnBattlefield(player1, "Stern Proctor");
    }

    @Test
    @DisplayName("ETB returns a target enchantment to its owner's hand")
    void etbReturnsEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        castSternProctor(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInHand(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("ETB cannot target a creature")
    void etbRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new SternProctor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    private void castSternProctor(UUID targetId) {
        harness.setHand(player1, List.of(new SternProctor()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, List.of(targetId));
    }
}
