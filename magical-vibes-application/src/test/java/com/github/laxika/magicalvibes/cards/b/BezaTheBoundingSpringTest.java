package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BezaTheBoundingSpring.class, Forest.class, GrizzlyBears.class})
class BezaTheBoundingSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Applies every bonus based on comparisons at resolution")
    void appliesEveryBonusAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BezaTheBoundingSpring()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setLife(player2, 21);
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.FISH)))
                .hasSize(2);
        harness.assertLife(player1, 24);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not apply a bonus when no opponent is ahead")
    void noBonusWhenNoOpponentIsAhead() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BezaTheBoundingSpring()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.FISH)))
                .isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
