package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConiferStrider.class, Shock.class})
class ConiferStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with hexproof")
    void entersWithHexproof() {
        harness.setHand(player1, List.of(new ConiferStrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent strider = findPermanent(player1, "Conifer Strider");
        assertThat(gqs.hasKeyword(gd, strider, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("An opponent cannot target it with a spell")
    void opponentCannotTargetIt() {
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new ConiferStrider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, strider.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
