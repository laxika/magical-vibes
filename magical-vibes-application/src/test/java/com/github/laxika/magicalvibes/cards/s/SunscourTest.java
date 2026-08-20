package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunscourTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreature permanents alone")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WallOfWood());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Sunscour()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Wall of Wood");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Can be cast by exiling two white cards from hand")
    void castsByExilingTwoWhiteCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Sunscour(), new SavannahLions(), new HealingSalve()));
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, 1, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Savannah Lions", "Healing Salve");
    }

    @Test
    @DisplayName("Alternate cost requires exactly two matching hand cards")
    void alternateCostRequiresTwoWhiteCards() {
        harness.setHand(player1, List.of(new Sunscour(), new SavannahLions()));
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, 1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
