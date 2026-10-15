package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenBirth.class, Cancel.class})
class MoltenBirthTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MoltenBirth()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
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

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Molten Birth")))
                .hasSize(1);

        if (wonFlip()) {
            harness.assertInHand(player1, "Molten Birth");
            harness.assertNotInGraveyard(player1, "Molten Birth");
        } else {
            harness.assertNotInHand(player1, "Molten Birth");
            harness.assertInGraveyard(player1, "Molten Birth");
        }
    }

    @Test
    @DisplayName("Countered Molten Birth creates no tokens and does not flip a coin")
    void counteredSpellDoesNotCreateTokensOrFlipCoin() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        MoltenBirth spell = new MoltenBirth();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
        assertThat(findPermanents(player2, "Elemental")).isEmpty();
        harness.assertInGraveyard(player1, "Molten Birth");
        harness.assertNotInHand(player1, "Molten Birth");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("coin flip"));
    }
}
