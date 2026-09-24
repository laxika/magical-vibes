package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Moderation.class, GrizzlyBears.class})
class ModerationTest extends BaseCardTest {

    @Test
    @DisplayName("Controller draws a card whenever they cast a spell")
    void controllerDrawsWhenCastingSpell() {
        harness.addToBattlefield(player1, new Moderation());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int handBeforeCast = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCast);
    }

    @Test
    @DisplayName("Controller cannot cast more than one spell each turn")
    void controllerCannotCastSecondSpell() {
        harness.addToBattlefield(player1, new Moderation());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Moderation does not restrict an opponent's spells")
    void doesNotRestrictOpponent() {
        harness.addToBattlefield(player1, new Moderation());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
