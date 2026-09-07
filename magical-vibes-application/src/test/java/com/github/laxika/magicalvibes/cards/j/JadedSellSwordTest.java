package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
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

@CardUsed({JadedSellSword.class, WilyGoblin.class})
class JadedSellSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Does not gain first strike or haste when cast without Treasure mana")
    void doesNotGainKeywordsWithoutTreasureMana() {
        harness.setHand(player1, List.of(new JadedSellSword()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sellSword = findPermanent(player1, "Jaded Sell-Sword");
        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Gains first strike and haste when Treasure mana was spent to cast it")
    void gainsKeywordsWithTreasureMana() {
        Permanent sellSword = castWithTreasureMana();

        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("First strike and haste wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent sellSword = castWithTreasureMana();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.HASTE)).isFalse();
    }

    private Permanent castWithTreasureMana() {
        harness.setHand(player1, List.of(new WilyGoblin(), new JadedSellSword()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Jaded Sell-Sword");
    }
}
