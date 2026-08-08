package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunspireGatekeepersTest extends BaseCardTest {

    @Test
    @DisplayName("With two Gates, ETB creates a 2/2 white Knight token with vigilance")
    void twoGatesCreatesKnightToken() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> knights = knightTokens(player1);
        assertThat(knights).hasSize(1);
        Permanent knight = knights.getFirst();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("With only one Gate the trigger does not fire")
    void oneGateDoesNotTrigger() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(knightTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Gates controlled by an opponent do not count")
    void opponentGatesDoNotCount() {
        harness.addToBattlefield(player2, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new BorosGuildgate());
        castGatekeepers();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(knightTokens(player1)).isEmpty();
        harness.assertOnBattlefield(player1, "Sunspire Gatekeepers");
    }

    private void castGatekeepers() {
        harness.setHand(player1, List.of(new SunspireGatekeepers()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
    }

    private List<Permanent> knightTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight"))
                .toList();
    }
}
