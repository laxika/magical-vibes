package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FavoredOfIroas.class, GloriousAnthem.class, GrizzlyBears.class})
class FavoredOfIroasTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry grants it double strike until end of turn")
    void ownEntryGrantsDoubleStrike() {
        castFavoredOfIroas();

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent favored = findFavoredOfIroas();
        assertThat(gqs.hasKeyword(gd, favored, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Another enchantment entering under your control grants it double strike")
    void anotherEnchantmentEntryGrantsDoubleStrike() {
        Permanent favored = harness.addToBattlefieldAndReturn(player1, new FavoredOfIroas());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, favored, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A non-enchantment entering under your control does not trigger it")
    void nonEnchantmentEntryDoesNotTrigger() {
        Permanent favored = harness.addToBattlefieldAndReturn(player1, new FavoredOfIroas());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, favored, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        Permanent favored = harness.addToBattlefieldAndReturn(player1, new FavoredOfIroas());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, favored, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Double strike wears off at the end of the turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent favored = harness.addToBattlefieldAndReturn(player1, new FavoredOfIroas());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, favored, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, favored, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void castFavoredOfIroas() {
        harness.setHand(player1, List.of(new FavoredOfIroas()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private Permanent findFavoredOfIroas() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FavoredOfIroas)
                .findFirst()
                .orElseThrow();
    }
}
