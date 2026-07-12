package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent draws two cards")
    void targetOpponentDrawsTwo() {
        harness.setHand(player1, List.of(new Inspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castInstant(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Inspiration");
    }

    @Test
    @DisplayName("Caster can target themselves to draw two cards")
    void targetSelfDrawsTwo() {
        harness.setHand(player1, List.of(new Inspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        // Started with one card (Inspiration), drew two, cast one.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 2);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Inspiration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID bearId = bear.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }
}
