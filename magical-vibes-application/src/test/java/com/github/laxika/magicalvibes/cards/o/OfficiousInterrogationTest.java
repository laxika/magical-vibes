package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OfficiousInterrogation.class, GrizzlyBears.class})
class OfficiousInterrogationTest extends BaseCardTest {

    @Test
    void investigatesForCreaturesControlledByTheTargetPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(List.of(player2.getId()), 1);

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void sumsCreaturesControlledByAllChosenPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(List.of(player2.getId(), player1.getId()), 2);

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }

    @Test
    void canChooseNoTargetPlayers() {
        cast(List.of(), 1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void cast(List<UUID> targetPlayerIds, int manaPerColor) {
        harness.setHand(player1, List.of(new OfficiousInterrogation()));
        harness.addMana(player1, ManaColor.WHITE, manaPerColor);
        harness.addMana(player1, ManaColor.BLUE, manaPerColor);
        harness.castInstant(player1, 0, targetPlayerIds);
        harness.passBothPriorities();
    }
}
