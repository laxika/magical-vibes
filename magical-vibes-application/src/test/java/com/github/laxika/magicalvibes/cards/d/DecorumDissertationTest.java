package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DecorumDissertationTest extends BaseCardTest {

    

    @Test
    @DisplayName("Target player draws two cards and loses 2 life")
    void targetPlayerDrawsTwoCardsAndLoses2Life() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castDecorumDissertationTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castDecorumDissertationTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DecorumDissertation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
