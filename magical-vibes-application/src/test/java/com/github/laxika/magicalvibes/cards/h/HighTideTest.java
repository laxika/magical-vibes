package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighTide.class, Island.class, Mountain.class})
class HighTideTest extends BaseCardTest {

    @Test
    @DisplayName("An Island tapped for mana adds an additional {U}")
    void addsBlueManaWhenIslandIsTapped() {
        harness.setHand(player1, List.of(new HighTide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player1, new Island());

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The effect is symmetric and applies only to Islands")
    void appliesToOpponentsIslandsOnly() {
        harness.setHand(player1, List.of(new HighTide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The additional mana effect expires at the end of the turn")
    void expiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new HighTide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player1, new Island());

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent island = findPermanent(player1, "Island");
        island.untap();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
