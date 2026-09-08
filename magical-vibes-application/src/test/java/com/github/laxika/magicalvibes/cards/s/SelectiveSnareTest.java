package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectiveSnareTest extends BaseCardTest {

    @Test
    @DisplayName("returns only targeted creatures of the chosen type")
    void returnsOnlyChosenCreatureType() {
        Permanent firstGoblin = addCreatureReady(player2, new GoblinPiker());
        Permanent secondGoblin = addCreatureReady(player2, new GoblinPiker());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelectiveSnare()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorceryWithChosenCreatureType(player1, 0, 2, CardSubtype.GOBLIN,
                List.of(firstGoblin.getId(), secondGoblin.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Goblin Piker");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("requires the chosen type to match every target while casting")
    void rejectsTargetOfAnotherType() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelectiveSnare()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorceryWithChosenCreatureType(player1, 0, 1,
                CardSubtype.GOBLIN, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("chosen creature type");
    }
}
