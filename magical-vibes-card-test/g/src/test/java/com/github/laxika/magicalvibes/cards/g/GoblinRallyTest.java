package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinRally.class})
class GoblinRallyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creates four 1/1 Goblin tokens under the caster's control")
    void resolvingCreatesFourGoblins() {
        harness.setHand(player1, List.of(new GoblinRally()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, List.of());

        List<Permanent> goblins = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOBLIN))
                .toList();
        assertThat(goblins).hasSize(4);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().isToken()).isTrue();
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.isTapped()).isFalse();
            assertThat(goblin.isSummoningSick()).isTrue();
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Rally");
    }

    @Test
    @DisplayName("Goblin tokens are created only when the spell resolves")
    void tokensAreNotCreatedWhileSpellIsOnStack() {
        harness.setHand(player1, List.of(new GoblinRally()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Rally");
    }
}
