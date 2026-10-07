package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunspireGatekeepers.class, AzoriusGuildgate.class, BorosGuildgate.class})
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

    @Test
    @DisplayName("Two Gates with the same name count and create one white Knight creature token")
    void sameNamedGatesCount() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        castGatekeepers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(knightTokens(player1)).hasSize(1);
        Permanent knight = knightTokens(player1).getFirst();
        assertThat(knight.getCard().isToken()).isTrue();
        assertThat(knight.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(knight.isTapped()).isFalse();
        assertThat(knightTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("More than two Gates still creates only one Knight token")
    void threeGatesCreateOneToken() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        castGatekeepers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(knightTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Gate count is checked on entry rather than when Gatekeepers is cast")
    void secondGateArrivingBeforeEntryEnablesTrigger() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        castGatekeepers();
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(knightTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Losing a Gate before the trigger resolves prevents token creation")
    void losingGateBeforeResolutionPreventsToken() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BorosGuildgate());
        castGatekeepers();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(gate);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(knightTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("A replacement Gate satisfies the condition when the trigger resolves")
    void replacementGateCountsAtResolution() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BorosGuildgate());
        castGatekeepers();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(gate);
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.passBothPriorities();

        assertThat(knightTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Removing Gatekeepers does not stop its already triggered ability")
    void abilityResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        castGatekeepers();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Sunspire Gatekeepers"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sunspire Gatekeepers");
        assertThat(knightTokens(player1)).hasSize(1);
    }

    private void castGatekeepers() {
        harness.castFromHand(player1, new SunspireGatekeepers(), "{3}{W}");
    }

    private List<Permanent> knightTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight"))
                .toList();
    }
}
