package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceFracture.class, GiantSpider.class, GrizzlyBears.class, Island.class})
class EssenceFractureTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoTargetCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        castEssenceFracture(List.of(bearsId, spiderId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID islandId = harness.getPermanentId(player1, "Island");

        assertThatThrownBy(() -> castEssenceFracture(List.of(bearsId, islandId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cycling discards Essence Fracture and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new EssenceFracture()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Essence Fracture");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castEssenceFracture(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new EssenceFracture()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetIds);
    }
}
