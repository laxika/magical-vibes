package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpectralProcessionTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Resolving Spectral Procession creates three Spirit tokens")
    void resolvingCreatesThreeTokens() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new SpectralProcession()));
        harness.addMana(player1, ManaColor.WHITE, 3); // {2/W}{2/W}{2/W} paid with three white

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .toList();
        assertThat(tokens).hasSize(3);
    }

    @Test
    @DisplayName("Created tokens are 1/1 white Spirits with flying")
    void tokensHaveCorrectStats() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new SpectralProcession()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .toList();

        assertThat(tokens).hasSize(3);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Spectral Procession goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new SpectralProcession()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spectral Procession"));
    }
}
