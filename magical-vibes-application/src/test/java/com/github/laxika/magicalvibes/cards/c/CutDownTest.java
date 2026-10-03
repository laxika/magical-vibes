package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutDown.class, GoblinPiker.class, GiantSpider.class, GrizzlyBears.class})
class CutDownTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with total power and toughness of exactly 5")
    void destroysCreatureAtThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());
        target.setPowerModifier(2);
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = target.getId();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertInGraveyard(player2, "Goblin Piker");
    }

    @Test
    @DisplayName("Destroys a creature with total power and toughness below 5")
    void destroysCreatureBelowThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = target.getId();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with total power and toughness greater than 5")
    void cannotTargetCreatureAboveThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = target.getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power and toughness 5 or less");
    }
}
