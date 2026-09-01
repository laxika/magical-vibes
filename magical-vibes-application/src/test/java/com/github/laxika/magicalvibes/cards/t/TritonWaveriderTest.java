package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
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

@CardUsed({TritonWaverider.class, GloriousAnthem.class})
class TritonWaveriderTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control grants flying until end of turn")
    void enchantmentTriggerGrantsFlying() {
        Permanent waverider = addCreatureReady(player1, new TritonWaverider());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent waverider = addCreatureReady(player1, new TritonWaverider());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentDoesNotTrigger() {
        Permanent waverider = addCreatureReady(player1, new TritonWaverider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castEnchantment(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isFalse();
    }
}
