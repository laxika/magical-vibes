package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinRallyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creates four 1/1 Goblin tokens under the caster's control")
    void resolvingCreatesFourGoblins() {
        harness.setHand(player1, List.of(new GoblinRally()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> goblins = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOBLIN))
                .toList();
        assertThat(goblins).hasSize(4);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Rally");
    }
}
