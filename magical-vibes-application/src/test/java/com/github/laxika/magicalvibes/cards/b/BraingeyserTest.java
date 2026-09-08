package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Braingeyser.class, GrizzlyBears.class})
class BraingeyserTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent draws X cards")
    void targetOpponentDrawsXCards() {
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
    }

    @Test
    void targetPlayerCanBeCaster() {
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        int handBefore = gd.playerHands.get(player1.getId()).size() - 1;
        harness.castSorcery(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("X=0 draws no cards")
    void xZeroDrawsNoCards() {
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castSorcery(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Braingeyser()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearId = bear.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }
}
