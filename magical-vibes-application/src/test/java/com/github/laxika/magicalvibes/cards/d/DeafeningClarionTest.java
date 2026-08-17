package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeafeningClarionTest extends BaseCardTest {

    @Test
    @DisplayName("The damage mode deals 3 damage to each creature")
    void dealsDamageToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        cast(new int[]{0});

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The lifelink mode grants lifelink to your creatures only until end of turn")
    void grantsLifelinkUntilEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        cast(new int[]{1});

        var ownBear = findPermanent(player1, "Grizzly Bears");
        var opponentBear = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.LIFELINK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Choosing both modes deals damage and grants lifelink")
    void choosesBothModes() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        cast(new int[]{0, 1});

        var ownGiant = findPermanent(player1, "Hill Giant");
        assertThat(gqs.hasKeyword(gd, ownGiant, Keyword.LIFELINK)).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void cast(int[] modes) {
        harness.setHand(player1, List.of(new DeafeningClarion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
