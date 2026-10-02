package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mulldrifter.class, Island.class})
class MulldrifterTest extends BaseCardTest {

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast: ETB draws two cards and Mulldrifter stays")
    void hardcastDrawsTwoAndStays() {
        harness.setHand(player1, List.of(new Mulldrifter()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Mulldrifter");
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: paying only {2}{U}, ETB still draws two and Mulldrifter is sacrificed")
    void evokeDrawsTwoAndSacrificesSelf() {
        harness.setHand(player1, List.of(new Mulldrifter()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Mulldrifter");
        harness.assertInGraveyard(player1, "Mulldrifter");
    }
}
