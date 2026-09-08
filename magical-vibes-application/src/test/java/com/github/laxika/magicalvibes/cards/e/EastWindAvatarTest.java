package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EastWindAvatar.class, GrizzlyBears.class})
class EastWindAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        harness.addToBattlefield(player1, new EastWindAvatar());
        Permanent avatar = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noBoostWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new EastWindAvatar());
        Permanent avatar = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castGrizzlyBears(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost is cumulative across multiple creature entries")
    void boostStacksForMultipleCreatures() {
        harness.addToBattlefield(player1, new EastWindAvatar());
        Permanent avatar = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(3);

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new EastWindAvatar());
        Permanent avatar = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    private void castGrizzlyBears(Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
