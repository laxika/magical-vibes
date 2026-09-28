package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldritchPact.class, Forest.class, GrizzlyBears.class})
class EldritchPactTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws and loses life equal to cards in their graveyard")
    void targetPlayerDrawsAndLosesLifeForTheirGraveyard() {
        List<Card> drawnCards = List.of(new Forest(), new Forest(), new Forest());
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, drawnCards);
        harness.setHand(player1, List.of(new EldritchPact()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a non-player")
    void cannotTargetNonPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EldritchPact()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        var creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
