package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HerdGnarr.class, AshcoatBear.class})
class HerdGnarrTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        harness.addToBattlefield(player1, new HerdGnarr());
        Permanent gnarr = gd.playerBattlefields.get(player1.getId()).getFirst();

        castAshcoatBear(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gnarr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gnarr)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noBoostWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new HerdGnarr());
        Permanent gnarr = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castAshcoatBear(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gnarr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnarr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HerdGnarr());
        Permanent gnarr = gd.playerBattlefields.get(player1.getId()).getFirst();

        castAshcoatBear(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gnarr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gnarr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger from its own entry")
    void doesNotTriggerFromItsOwnEntry() {
        harness.addToBattlefield(player1, new HerdGnarr());
        Permanent existingGnarr = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castFromHand(player1, new HerdGnarr(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent enteringGnarr = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gqs.getEffectivePower(gd, existingGnarr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, existingGnarr)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, enteringGnarr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enteringGnarr)).isEqualTo(2);
    }

    private void castAshcoatBear(Player player) {
        harness.castFromHand(player, new AshcoatBear(), "{1}{G}");
    }
}
