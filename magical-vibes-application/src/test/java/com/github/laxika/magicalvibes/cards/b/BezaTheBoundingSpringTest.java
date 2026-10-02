package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HiredClaw;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BezaTheBoundingSpring.class, Forest.class, HiredClaw.class})
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
        harness.addToBattlefield(player2, new HiredClaw());
        harness.addToBattlefield(player2, new HiredClaw());
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

    @ParameterizedTest
    @CsvSource({
            "true, false, false, false",
            "false, true, false, false",
            "false, false, true, false",
            "false, false, false, true"
    })
    @DisplayName("Each bonus applies independently of all other comparisons")
    void bonusesAreIndependent(boolean lands, boolean life, boolean creatures, boolean hand) {
        harness.setLife(player1, 20);
        harness.setLife(player2, life ? 21 : 20);
        harness.setHand(player1, List.of(new BezaTheBoundingSpring()));
        harness.setHand(player2, hand ? List.of(new Forest()) : List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        if (lands) {
            harness.addToBattlefield(player2, new Forest());
        }
        if (creatures) {
            harness.addToBattlefield(player2, new HiredClaw());
            harness.addToBattlefield(player2, new HiredClaw());
        }
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(lands ? 1 : 0);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.FISH)))
                .hasSize(creatures ? 2 : 0);
        harness.assertLife(player1, life ? 24 : 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand ? 1 : 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand ? 1 : 0);
        harness.assertLife(player2, life ? 21 : 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tied comparisons grant no bonuses and Beza counts as your creature")
    void tiedComparisonsGrantNoBonuses() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BezaTheBoundingSpring(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new HiredClaw());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Bonuses are lost if the opponent is no longer ahead at resolution")
    void rechecksComparisonsAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 21);
        harness.setHand(player1, List.of(new BezaTheBoundingSpring()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new HiredClaw());
        harness.addToBattlefield(player2, new HiredClaw());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new HiredClaw());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.FISH)))
                .isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
