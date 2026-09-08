package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkEndurance.class, GrizzlyBears.class})
class DarkEnduranceTest extends BaseCardTest {

    @Test
    @DisplayName("Costs only {B} when targeting a blocking creature and grants +2/+0 and indestructible")
    void boostsBlockingCreatureAtReducedCost() {
        Permanent blocker = addBlockingBear(player2);
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and indestructible wear off at cleanup")
    void effectsWearOff() {
        Permanent blocker = addBlockingBear(player2);
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot pay only the reduced cost when targeting a nonblocking creature")
    void reducedCostDoesNotApplyToNonblockingCreature() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setupSpell();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupSpell() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkEndurance()));
    }

    private Permanent addBlockingBear(Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.setSummoningSick(false);
        bear.setBlocking(true);
        return bear;
    }
}
