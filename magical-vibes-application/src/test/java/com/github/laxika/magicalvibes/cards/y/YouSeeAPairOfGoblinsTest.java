package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouSeeAPairOfGoblins.class, GrizzlyBears.class})
class YouSeeAPairOfGoblinsTest extends BaseCardTest {

    @Test
    @DisplayName("Charge Them gives your creatures +2/+0 until end of turn")
    void chargesOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Charge Them wears off at cleanup")
    void chargeBonusWearsOffAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Befriend Them creates two 1/1 red Goblin tokens")
    void befriendsGoblins() {
        cast(1);

        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(2);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        });
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new YouSeeAPairOfGoblins()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstant(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
