package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GavonyTrapper;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunsetRevelry.class, GavonyTrapper.class, Island.class})
class SunsetRevelryTest extends BaseCardTest {

    private long humanTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .count();
    }

    private void cast() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Applies all three effects when an opponent is ahead in all three resources")
    void appliesAllEffects() {
        Island drawn = new Island();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GavonyTrapper());
        harness.setHand(player1, List.of(new SunsetRevelry()));
        harness.setHand(player2, List.of(new GavonyTrapper(), new GavonyTrapper()));
        harness.setLibrary(player1, List.of(drawn));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(humanTokenCount()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Does not apply any effect when the opponent is not ahead")
    void appliesNoEffectsWhenOpponentIsNotAhead() {
        GavonyTrapper kept = new GavonyTrapper();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SunsetRevelry(), kept));
        harness.setHand(player2, List.of(new GavonyTrapper()));
        harness.setLibrary(player1, List.of(new Island()));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
    }

    @Test
    @DisplayName("Resolves each clause independently")
    void resolvesEachClauseIndependently() {
        GavonyTrapper kept = new GavonyTrapper();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GavonyTrapper());
        harness.addToBattlefield(player2, new GavonyTrapper());
        harness.setHand(player1, List.of(new SunsetRevelry(), kept));
        harness.setHand(player2, List.of(new GavonyTrapper()));
        harness.setLibrary(player1, List.of(new Island()));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(humanTokenCount()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
    }

    @Test
    void gainsLifeWithoutCreatingTokensOrDrawing() {
        harness.setLife(player1, 19);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SunsetRevelry()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void createsTokensWithoutGainingLifeOrDrawing() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 19);
        harness.addToBattlefield(player2, new GavonyTrapper());
        harness.setHand(player1, List.of(new SunsetRevelry()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void castingRevelryBreaksHandSizeTieAndDrawsWithoutOtherBenefits() {
        Island drawn = new Island();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SunsetRevelry()));
        harness.setHand(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(drawn));

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void checksResourcesAtResolutionRatherThanCasting() {
        Island drawn = new Island();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SunsetRevelry()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);

        harness.setLife(player1, 10);
        harness.addToBattlefield(player2, new GavonyTrapper());
        harness.setHand(player2, List.of(new Island()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(humanTokenCount()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNothingWhenResourcesBecomeEqualBeforeResolution() {
        Island kept = new Island();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GavonyTrapper());
        harness.setHand(player1, List.of(new SunsetRevelry(), kept));
        harness.setHand(player2, List.of(new Island(), new Island()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);

        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GavonyTrapper());
        harness.setHand(player2, List.of(new Island()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
    }
}
