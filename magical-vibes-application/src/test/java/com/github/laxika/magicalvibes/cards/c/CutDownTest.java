package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorderlandMarauder;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutDown.class, BorderlandMarauder.class, GiantSpider.class, GrizzlyBears.class})
class CutDownTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with total power and toughness of exactly 5")
    void destroysCreatureAtThreshold() {
        harness.addToBattlefield(player2, new BorderlandMarauder());
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = harness.getPermanentId(player2, "Borderland Marauder");

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Borderland Marauder");
        harness.assertInGraveyard(player2, "Borderland Marauder");
    }

    @Test
    @DisplayName("Destroys a creature with total power and toughness below 5")
    void destroysCreatureBelowThreshold() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with total power and toughness greater than 5")
    void cannotTargetCreatureAboveThreshold() {
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = harness.getPermanentId(player2, "Giant Spider");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power and toughness 5 or less");
    }
}
