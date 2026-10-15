package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimelyReinforcements.class, RuneclawBear.class})
class TimelyReinforcementsTest extends BaseCardTest {

    private long soldierTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .count();
    }

    private void cast() {
        harness.setHand(player1, List.of(new TimelyReinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Behind on both life and creatures: gains 6 life and creates three Soldiers")
    void bothHalvesApply() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new RuneclawBear());

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(soldierTokenCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Equal life and equal creature counts: neither half applies")
    void neitherHalfApplies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new RuneclawBear());

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(soldierTokenCount()).isZero();
    }

    @Test
    @DisplayName("Behind on life only: gains 6 life but creates no tokens")
    void lifeHalfOnly() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RuneclawBear());

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(soldierTokenCount()).isZero();
    }

    @Test
    @DisplayName("Behind on creatures only: creates three Soldiers but gains no life")
    void tokenHalfOnly() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player2, new RuneclawBear());

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(soldierTokenCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Conditions can become true after casting")
    void conditionsBecomeTrueBeforeResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TimelyReinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, 0);

        harness.setLife(player1, 19);
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(soldierTokenCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Conditions can become false after casting")
    void conditionsBecomeFalseBeforeResolution() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TimelyReinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, 0);

        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(soldierTokenCount()).isZero();
    }

    @Test
    @DisplayName("Created Soldiers are untapped white 1/1 creature tokens")
    void createsCorrectSoldierTokens() {
        harness.addToBattlefield(player2, new RuneclawBear());

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.isTapped()).isFalse();
        });
        harness.assertInGraveyard(player1, "Timely Reinforcements");
    }

    @Test
    @DisplayName("Existing creature tokens count when a second spell resolves")
    void existingTokensCountForCreatureComparison() {
        harness.setLife(player1, 19);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new RuneclawBear());

        cast();
        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(soldierTokenCount()).isEqualTo(3);
    }
}
