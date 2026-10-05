package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.p.ProsperousInnkeeper;
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

@CardUsed({JadedSellSword.class, ProsperousInnkeeper.class})
class JadedSellSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Does not gain first strike or haste when cast without Treasure mana")
    void doesNotGainKeywordsWithoutTreasureMana() {
        harness.setHand(player1, List.of(new JadedSellSword()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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

        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when no Treasure mana was spent")
    void doesNotTriggerWithoutTreasureMana() {
        harness.setHand(player1, List.of(new JadedSellSword()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jaded Sell-Sword");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Treasure mana of another color can pay the generic cost")
    void gainsKeywordsWithTreasureManaPayingGenericCost() {
        Permanent sellSword = castWithTreasureMana("BLUE");

        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sellSword, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Only the Sell-Sword gains the keywords")
    void doesNotGrantKeywordsToOtherCreatures() {
        castWithTreasureMana();

        Permanent innkeeper = findPermanent(player1, "Prosperous Innkeeper");
        assertThat(gqs.hasKeyword(gd, innkeeper, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, innkeeper, Keyword.HASTE)).isFalse();
    }

    private Permanent castWithTreasureMana() {
        return castWithTreasureMana("RED");
    }

    private Permanent castWithTreasureMana(String treasureColor) {
        harness.setHand(player1, List.of(new ProsperousInnkeeper(), new JadedSellSword()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, treasureColor);
        harness.addMana(player1, ManaColor.COLORLESS, "RED".equals(treasureColor) ? 3 : 2);
        if (!"RED".equals(treasureColor)) {
            harness.addMana(player1, ManaColor.RED, 1);
        }

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return findPermanent(player1, "Jaded Sell-Sword");
    }
}
