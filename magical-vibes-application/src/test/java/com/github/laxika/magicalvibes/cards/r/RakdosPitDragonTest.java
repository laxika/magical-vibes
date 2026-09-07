package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RakdosPitDragon.class, GrizzlyBears.class})
class RakdosPitDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Gains flying until end of turn")
    void gainsFlyingUntilEndOfTurn() {
        Permanent dragon = addReadyDragon();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 until end of turn")
    void getsPlusOnePowerUntilEndOfTurn() {
        Permanent dragon = addReadyDragon();
        int basePower = gqs.getEffectivePower(gd, dragon);
        int baseToughness = gqs.getEffectiveToughness(gd, dragon);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Has double strike with an empty hand")
    void hasDoubleStrikeWithEmptyHand() {
        harness.setHand(player1, List.of());
        Permanent dragon = addReadyDragon();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses double strike when a card enters its controller's hand")
    void losesDoubleStrikeWhenHandGrows() {
        harness.setHand(player1, List.of());
        Permanent dragon = addReadyDragon();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addReadyDragon() {
        Permanent dragon = new Permanent(new RakdosPitDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dragon);
        return dragon;
    }
}
