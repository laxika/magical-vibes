package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GruulScrapper.class})
class GruulScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Gains haste until end of turn when red mana was spent to cast it")
    void gainsHasteWhenRedManaWasSpent() {
        Permanent scrapper = castScrapper(true);

        assertThat(gqs.hasKeyword(gd, scrapper, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scrapper, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not gain haste when red mana was not spent to cast it")
    void doesNotGainHasteWithoutRedMana() {
        Permanent scrapper = castScrapper(false);

        assertThat(gqs.hasKeyword(gd, scrapper, Keyword.HASTE)).isFalse();
    }

    private Permanent castScrapper(boolean spendRedMana) {
        harness.setHand(player1, List.of(new GruulScrapper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, spendRedMana ? 2 : 3);
        if (spendRedMana) {
            harness.addMana(player1, ManaColor.RED, 1);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GruulScrapper)
                .findFirst()
                .orElseThrow();
    }
}
