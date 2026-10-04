package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FinaleOfGlory.class)
class FinaleOfGloryTest extends BaseCardTest {

    @Test
    void belowTenCreatesSoldiersOnly() {
        harness.setHand(player1, List.of(new FinaleOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 12);

        harness.castSorcery(player1, 0, 10 - 1);
        harness.passBothPriorities();

        assertThat(tokensNamed("Soldier")).hasSize(9);
        assertThat(tokensNamed("Angel")).isEmpty();
        assertThat(tokensNamed("Soldier")).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
            assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        });
    }

    @Test
    void tenCreatesSoldiersAndAngels() {
        harness.setHand(player1, List.of(new FinaleOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 12);

        harness.castSorcery(player1, 0, 10);
        harness.passBothPriorities();

        assertThat(tokensNamed("Soldier")).hasSize(10);
        assertThat(tokensNamed("Angel")).hasSize(10);
        assertThat(tokensNamed("Angel")).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
            assertThat(token.getCard().getKeywords())
                    .contains(Keyword.FLYING, Keyword.VIGILANCE);
        });
    }

    private List<Permanent> tokensNamed(String name) {
        return findPermanents(player1, name).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    @Test
    void zeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new FinaleOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FinaleOfGlory);
    }

    @Test
    void aboveTenCreatesXOfEachTokenForTheController() {
        harness.setHand(player1, List.of(new FinaleOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 13);

        harness.castSorcery(player1, 0, 11);
        harness.passBothPriorities();

        assertThat(tokensNamed("Soldier")).hasSize(11);
        assertThat(tokensNamed("Angel")).hasSize(11);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(22)
                .allSatisfy(token -> assertThat(token.isTapped()).isFalse());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
