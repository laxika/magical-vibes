package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AggressiveSabotage.class, GrizzlyBears.class})
class AggressiveSabotageTest extends BaseCardTest {

    @Test
    void targetPlayerDiscardsTwoCardsWithoutKicker() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void kickedSpellAlsoDealsThreeDamageToTargetPlayer() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player2.getId());
        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(),
                false, null, null, null, null, null, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void cannotTargetAPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var permanentId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new AggressiveSabotage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }
}
