package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalametWarScribe.class, GrizzlyBears.class})
class MalametWarScribeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering gives creatures you control +2/+1 until end of turn")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectiveToughness()).isEqualTo(3);
        assertThat(findPermanent(player1, "Malamet War Scribe").getEffectivePower()).isEqualTo(6);
        assertThat(findPermanent(player1, "Malamet War Scribe").getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(player1);

        assertThat(findPermanent(player2, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player2, "Grizzly Bears").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanent(player1, "Malamet War Scribe").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Malamet War Scribe").getEffectiveToughness()).isEqualTo(3);
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new MalametWarScribe()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
