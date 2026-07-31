package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoltenBirthTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MoltenBirth()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private boolean wonFlip() {
        return gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip"));
    }

    @Test
    @DisplayName("Cast creates two 1/1 Elemental tokens regardless of the flip")
    void createsTwoElementalTokens() {
        cast();

        assertThat(gd.stack).isEmpty();
        List<Permanent> tokens = findPermanents(player1, "Elemental");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> {
            assertThat(t.getEffectivePower()).isEqualTo(1);
            assertThat(t.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Returns itself to hand on a won flip, otherwise goes to the graveyard")
    void coinFlipDecidesReturnToHand() {
        cast();

        boolean inHand = gd.playerHands.get(player1.getId()).stream()
                .anyMatch(card -> "Molten Birth".equals(card.getName()));
        boolean inGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(card -> "Molten Birth".equals(card.getName()));

        if (wonFlip()) {
            assertThat(inHand).isTrue();
            assertThat(inGraveyard).isFalse();
        } else {
            assertThat(inHand).isFalse();
            assertThat(inGraveyard).isTrue();
        }
    }
}
