package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallTheCoppercoats.class, GrizzlyBears.class})
class CallTheCoppercoatsTest extends BaseCardTest {

    @Test
    void createsOneSoldierForEachCreatureControlledByTargetOpponent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(List.of(player2.getId()), 1);

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(2);
    }

    @Test
    void canChooseNoTargetOpponents() {
        cast(List.of(), 1);

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new CallTheCoppercoats()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetPlayerIds, int whiteMana) {
        harness.setHand(player1, List.of(new CallTheCoppercoats()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);
        harness.addMana(player1, ManaColor.COLORLESS, whiteMana + 1);
        harness.castInstant(player1, 0, targetPlayerIds);
        harness.passBothPriorities();
    }
}
