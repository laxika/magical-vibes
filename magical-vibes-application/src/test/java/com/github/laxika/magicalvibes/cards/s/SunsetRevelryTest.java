package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunsetRevelry.class, GrizzlyBears.class, Island.class})
class SunsetRevelryTest extends BaseCardTest {

    private long humanTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .count();
    }

    private void cast() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Applies all three effects when an opponent is ahead in all three resources")
    void appliesAllEffects() {
        Island drawn = new Island();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SunsetRevelry()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(drawn));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(humanTokenCount()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Does not apply any effect when the opponent is not ahead")
    void appliesNoEffectsWhenOpponentIsNotAhead() {
        GrizzlyBears kept = new GrizzlyBears();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SunsetRevelry(), kept));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island()));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
    }

    @Test
    @DisplayName("Resolves each clause independently")
    void resolvesEachClauseIndependently() {
        GrizzlyBears kept = new GrizzlyBears();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SunsetRevelry(), kept));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island()));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(humanTokenCount()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
    }
}
