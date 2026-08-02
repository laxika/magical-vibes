package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BalustradeSpyTest extends BaseCardTest {

    private void castBalustradeSpy(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new BalustradeSpy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB mills target player until the first land, including that land")
    void millsTargetPlayerUntilFirstLand() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
                new GrizzlyBears(),
                new Divination(),
                new Forest(),
                new GrizzlyBears()
        ));

        castBalustradeSpy(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Grizzly Bears", "Divination", "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name")
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can target its controller")
    void canTargetController() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(),
                new Forest(),
                new Divination()
        ));

        castBalustradeSpy(player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Grizzly Bears", "Forest");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting("name")
                .containsExactly("Divination");
    }
}
