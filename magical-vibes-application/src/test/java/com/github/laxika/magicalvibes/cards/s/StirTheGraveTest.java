package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StirTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature card with mana value equal to X from the graveyard")
    void returnsCreatureWithManaValueEqualToX() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears())); // MV 2
        UUID bearsId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=2 + {B}

        harness.castSorcery(player1, 0, 2, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Rejects a creature card with mana value greater than X")
    void rejectsManaValueAboveX() {
        harness.setGraveyard(player1, List.of(new SerraAngel())); // MV 5
        UUID angelId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=2

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, angelId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature card in the graveyard")
    void rejectsNoncreatureCard() {
        harness.setGraveyard(player1, List.of(new StirTheGrave()));
        UUID sorceryId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, sorceryId))
                .isInstanceOf(IllegalStateException.class);
    }
}
