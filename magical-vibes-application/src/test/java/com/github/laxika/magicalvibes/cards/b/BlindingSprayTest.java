package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlindingSpray.class, GrizzlyBears.class})
class BlindingSprayTest extends BaseCardTest {

    @Test
    @DisplayName("Gives opponents' creatures -4/-0 and draws a card")
    void weakensOpponentsCreaturesAndDraws() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingSpray()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectivePower()).isEqualTo(-2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingSpray()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(opponent.getEffectivePower()).isEqualTo(-2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(opponent.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws exactly one card even when the opponent controls no creatures")
    void drawsWithoutOpposingCreatures() {
        harness.setHand(player1, List.of(new BlindingSpray()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Weakens every opposing creature, including one entering while the spell is on the stack")
    void affectsCreaturesPresentAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingSpray()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(-2);
        assertThat(second.getEffectivePower()).isEqualTo(-2);
        assertThat(first.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not weaken creatures entering after resolution")
    void doesNotAffectLaterCreatures() {
        Permanent existing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlindingSpray()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);
        Permanent later = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(existing.getEffectivePower()).isEqualTo(-2);
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }
}
